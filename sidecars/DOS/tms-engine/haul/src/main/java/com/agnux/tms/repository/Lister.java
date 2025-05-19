package com.agnux.tms.repository;

import lombok.Getter;
import lombok.AllArgsConstructor;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
abstract class Lister<T> {

    private static final String AND_SORROUNDED_BY_SPACES = " AND ";

    @Getter
    @AllArgsConstructor
    public static class Param {

        final String name;
        final String value;
    }

    @Getter
    @AllArgsConstructor
    public static class Result<T> {

        private final int code;
        private final String message;
        private final List<T> data;
        private final int totalItems;
        private final int totalPages;
    }

    private final String tableName;
    private final List<String> selectFields;
    private final Set<String> quotedFields;

    // Subclass must implement how to map a ResultSet row to T
    protected abstract T mapRow(ResultSet rs) throws SQLException;

    public Result<T> list(Connection conn, Map<String, String> filters, Map<String, String> pagination) {
        List<Param> filterParams = filters.entrySet().stream()
                .map(e -> new Param(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        List<Param> paginationParams = pagination.entrySet().stream()
                .map(e -> new Param(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        return list(conn, filterParams, paginationParams);
    }

    public Result<T> list(Connection conn, List<Param> searchParams, List<Param> pageParams) {
        String conditionStr = buildCondition(searchParams);
        Map<String, String> pageMap = pageParams.stream()
                .collect(Collectors.toMap(Param::getName, Param::getValue));

        PaginationHelper.PageInfo pageInfo = PaginationHelper.extractPageInfo(pageMap);

        String countByField = Optional.ofNullable(pageInfo.getOrderBy())
                .filter(f -> !f.isBlank())
                .orElse("id");

        int totalItems;
        try {
            totalItems = EntityCounter.countEntities(conn, tableName, conditionStr, true, countByField);
        } catch (SQLException e) {
            return new Result<>(-1, "Error counting entities: " + e.getMessage(), Collections.emptyList(), 0, 0);
        }

        int offset = pageInfo.getOffset();
        int limit = pageInfo.getLimit(totalItems);
        int totalPages = pageInfo.getTotalPages(totalItems);

        if (offset >= totalItems) {
            return new Result<>(-1, "Page " + pageInfo.getPage() + " does not exist", Collections.emptyList(), totalItems, totalPages);
        }

        try {
            List<T> items = fetchEntities(conn, conditionStr, pageInfo, limit, offset);
            return new Result<>(items.size(), "", items, totalItems, totalPages);
        } catch (SQLException e) {
            return new Result<>(-1, "Query execution error: " + e.getMessage(), Collections.emptyList(), totalItems, totalPages);
        }
    }

    private String buildCondition(List<Param> searchParams) {
        String condition = searchParams.stream()
                .map(param -> {
                    String value = quotedFields.contains(param.getName())
                            ? "'" + param.getValue() + "'"
                            : param.getValue();
                    return param.getName() + "=" + value;
                })
                .collect(Collectors.joining(AND_SORROUNDED_BY_SPACES));

        return condition.isBlank() ? "" : AND_SORROUNDED_BY_SPACES + condition;
    }

    private String buildSelectQuery(String conditionStr, PaginationHelper.PageInfo pageInfo, int limit, int offset) {
        return String.format("""
                SELECT %s
                  FROM %s
                 WHERE NOT blocked
                   %s
                 ORDER BY %s %s
                 LIMIT %d OFFSET %d
                """,
                String.join(", ", selectFields),
                tableName,
                conditionStr,
                pageInfo.getOrderBy(),
                pageInfo.getOrder(),
                limit,
                offset
        );
    }

    protected List<T> fetchEntities(Connection conn, String conditionStr, PaginationHelper.PageInfo pageInfo, int limit, int offset) throws SQLException {
        String sql = buildSelectQuery(conditionStr, pageInfo, limit, offset);
        List<T> items = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                items.add(mapRow(rs));
            }
        }

        return items;
    }

    private static class EntityCounter {

        public static class NoResultFoundException extends RuntimeException {

            public NoResultFoundException(String message) {
                super(message);
            }
        }

        public static class MultipleResultsFoundException extends RuntimeException {

            public MultipleResultsFoundException(String message) {
                super(message);
            }
        }

        public static int countEntities(Connection conn,
                String table,
                String searchParamsClause,
                boolean notBlocked,
                String countByField) throws SQLException {

            final String field = Optional.ofNullable(countByField)
                    .filter(f -> !f.isBlank())
                    .orElse("id");

            final String query = buildQuery(table, field, searchParamsClause, notBlocked);

            try (PreparedStatement stmt = conn.prepareStatement(query); ResultSet rs = stmt.executeQuery()) {

                if (!rs.next()) {
                    throw new NoResultFoundException("Expected exactly one result");
                }

                int total = rs.getInt("total");

                if (rs.next()) {
                    throw new MultipleResultsFoundException("Multiple results found, but only one expected");
                }

                return total;
            }
        }

        private static String buildQuery(String table, String field, String searchParamsClause, boolean notBlocked) {
            String base = String.format("SELECT COUNT(%s)::int AS total FROM %s WHERE TRUE", field, table);
            String blockClause = notBlocked ? " AND NOT blocked" : "";

            return new StringBuilder(base)
                    .append(blockClause)
                    .append(Optional.ofNullable(searchParamsClause)
                            .filter(s -> !s.isBlank())
                            .orElse(""))
                    .toString();
        }
    }
}
