package com.agnux.tms.repository;

import lombok.Getter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public abstract class Lister<T> {

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

    public Result<T> list(Connection conn, List<Param> searchParams, Map<String, String> pageParams) {
        String conditionStr = buildCondition(searchParams);
        PaginationHelper.PageInfo pageInfo = PaginationHelper.extractPageInfo(pageParams);

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

        String sql = buildSelectQuery(conditionStr, pageInfo, limit, offset);

        List<T> items = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                items.add(mapRow(rs));
            }

            return new Result<>(items.size(), "", items, totalItems, totalPages);

        } catch (SQLException e) {
            return new Result<>(-1, "Query execution error: " + e.getMessage(), Collections.emptyList(), totalItems, totalPages);
        }
    }

    private String buildCondition(List<Param> searchParams) {
        String condition = searchParams.stream()
                .map(param -> {
                    String value = quotedFields.contains(param.getName()) ? "'" + param.getValue() + "'" : param.getValue();
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

    // Subclass must implement how to map a ResultSet row to T
    protected abstract T mapRow(ResultSet rs) throws SQLException;
}
