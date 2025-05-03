package com.agnux.haul.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

class BasicRepoCommonHelper {

    protected static void verifyPgFunctionExists(Connection conn, String functionName) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT 1 FROM pg_proc WHERE proname = ?")) {
            stmt.setString(1, functionName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("PostgreSQL function '" + functionName + "' should exist");
                }
            }
        }
    }
}
