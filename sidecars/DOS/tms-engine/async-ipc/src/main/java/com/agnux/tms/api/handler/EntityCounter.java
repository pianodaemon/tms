package com.agnux.tms.api.handler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

class EntityCounter {

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

    private static int getSingleCount(ResultSet rs) {
        try {
            int total = rs.getInt("total");

            if (rs.next()) {
                throw new MultipleResultsFoundException("Multiple results found, but only one expected");
            }

            return total;
        } catch (SQLException e) {
            throw new RuntimeException("Error reading result set", e);
        }
    }
}
