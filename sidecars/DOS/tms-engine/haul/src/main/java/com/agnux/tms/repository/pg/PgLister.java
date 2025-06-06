package com.agnux.tms.repository.pg;

import com.agnux.tms.errors.ErrorCodes;
import com.agnux.tms.errors.TmsException;
import com.agnux.tms.repository.PaginationSegment;
import lombok.Getter;
import lombok.AllArgsConstructor;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;

@Log4j2
@AllArgsConstructor
class PgLister<T> {

    private static final String AND_SORROUNDED_BY_SPACES = " AND ";
    private static final String DEFAULT_COUNTABLE_FIELD = "id";
    private static final String[] SCHAR_OPERATOR_PREFIX = {"qu_", "LIKE"};
    private static final String[][] SCALAR_OPERATOR_PREFIXES = {
        {"eq_", "="},
        {"ne_", "!="},
        {"lt_", "<"},
        {"le_", "<="},
        {"gt_", ">"},
        {"ge_", ">="}
    };

    @Getter
    @AllArgsConstructor
    public static class Param {

        final String name;
        final String value;
    }

    @FunctionalInterface
    public interface RowMapper<T, R> {

        R apply(T t) throws SQLException;
    }

    private final String tableName;
    private final Set<String> quotedFields;
    private final List<String> selectFields;
    private final RowMapper<ResultSet, T> rowMapper;

    // Subclass must implement how to map a ResultSet row to T
    //protected abstract T mapRow(ResultSet rs) throws SQLException;
    public PaginationSegment<T> list(Connection conn, Map<String, String> searchParams, Map<String, String> pageParams) throws TmsException {
        List<Param> filterParams = searchParams.entrySet().stream()
                .map(e -> new Param(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        return list(conn, filterParams, pageParams);
    }

    private PaginationSegment<T> list(Connection conn, List<Param> searchParams, Map<String, String> pageParams) throws TmsException {
        String conditionStr = buildCondition(searchParams);
        PaginationHelper.PageInfo pageInfo = PaginationHelper.extractPageInfo(pageParams);

        String countByField = Optional.ofNullable(pageInfo.getOrderBy())
                .filter(f -> !f.isBlank())
                .orElse(DEFAULT_COUNTABLE_FIELD);

        int totalItems;
        try {
            totalItems = EntityCounter.countEntities(conn, tableName, conditionStr, true, countByField);
        } catch (SQLException e) {
            final String emsg = "Error counting entities";
            throw new TmsException(emsg, e, ErrorCodes.REPO_PROVIDER_ISSUES);
        }

        int offset = pageInfo.getOffset();
        int limit = pageInfo.getLimit(totalItems);
        int totalPages = pageInfo.getTotalPages(totalItems);

        if (offset >= totalItems) {
            final String emsg = "Page " + pageInfo.getPage() + " does not exist";
            throw new TmsException(emsg, ErrorCodes.REPO_PROVIDER_NONPRESENT_DATA);
        }

        List<T> items = fetchEntities(conn, conditionStr, pageInfo, limit, offset);
        return new PaginationSegment<>(items, totalItems, totalPages);
    }

    private String buildCondition(List<Param> searchParams) throws TmsException {
        String condition = searchParams.stream()
                .map(param -> {
                    if (param.getName().startsWith(SCHAR_OPERATOR_PREFIX[0])) {
                        String name = param.getName().substring(SCHAR_OPERATOR_PREFIX[0].length());
                        if (quotedFields.contains(name)) {
                            return String.format("%s %s '%s'", name, SCHAR_OPERATOR_PREFIX[1], param.getValue());
                        }
                        throw new RuntimeException(String.format("%s is not a quoted field", name));
                    }

                    String field = param.getName();
                    String operator = "";
                    for (String[] entry : SCALAR_OPERATOR_PREFIXES) {
                        if (param.getName().startsWith(entry[0])) {
                            operator = entry[1];
                            field = param.getName().substring(entry[0].length());
                            break;
                        }
                    }

                    return field + " " + operator + " " + param.getValue();
                }).collect(Collectors.joining(AND_SORROUNDED_BY_SPACES));

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

    protected List<T> fetchEntities(Connection conn, String conditionStr,
            PaginationHelper.PageInfo pageInfo, final int limit, final int offset) throws TmsException {
        String sql = buildSelectQuery(conditionStr, pageInfo, limit, offset);
        List<T> items = new ArrayList<>();

        log.debug("Fetching query : " + sql);
        try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                items.add(rowMapper.apply(rs));
            }
        } catch (SQLException ex) {
            throw new TmsException("Fetch execution error", ex, ErrorCodes.REPO_PROVIDER_ISSUES);
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
                    .orElse(DEFAULT_COUNTABLE_FIELD);

            final String query = buildQuery(table, field, searchParamsClause, notBlocked);
            log.debug("Counting query : " + query);
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

    private static class PaginationHelper {

        private static final int DEFAULT_PER_PAGE = 10;
        private static final int DEFAULT_PAGE = 1;
        private static final String DEFAULT_ORDER_BY = DEFAULT_COUNTABLE_FIELD;
        private static final String DEFAULT_ORDER = "ASC";

        @Getter
        @AllArgsConstructor
        public static class PageInfo {

            private final int perPage;
            private final int page;
            private final String orderBy;
            private final String order;

            public int getOffset() {
                return perPage * (page - 1);
            }

            public int getLimit(int totalItems) {
                return Math.min(perPage, Math.max(0, totalItems - getOffset()));
            }

            public int getTotalPages(int totalItems) {
                return (int) Math.ceil((double) totalItems / perPage);
            }
        }

        public static PageInfo extractPageInfo(Map<String, String> pageParams) {
            final int perPage = parseOrDefault(pageParams.get("size"), DEFAULT_PER_PAGE);
            final int page = parseOrDefault(pageParams.get("number"), DEFAULT_PAGE);
            final String orderBy = pageParams.getOrDefault("order_by", DEFAULT_ORDER_BY);
            final String order = pageParams.getOrDefault("order", DEFAULT_ORDER);

            return new PageInfo(perPage, page, orderBy, order);
        }

        private static Integer parseOrDefault(String value, int defaultVal) {
            try {
                return Integer.valueOf(value);
            } catch (NumberFormatException e) {
                return defaultVal;
            }
        }
    }
}
