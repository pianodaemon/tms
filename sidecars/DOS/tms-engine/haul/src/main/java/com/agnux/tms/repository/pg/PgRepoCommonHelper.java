package com.agnux.tms.repository.pg;

import com.agnux.tms.errors.ErrorCodes;
import com.agnux.tms.errors.TmsException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

class PgRepoCommonHelper {

    protected static final String FETCH_BY_ID_SQL_QUERY = "SELECT * FROM %s WHERE not blocked AND id = ?";

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

    protected static void blockAt(Connection conn, String table, UUID entityId) throws TmsException {
        String sql = String.format("UPDATE %s SET blocked = true WHERE id = ?", table);

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, entityId);
            int updates = stmt.executeUpdate();
            if (updates == 1) {
                return;
            }
            throw new TmsException("entity not updated", ErrorCodes.REPO_PROVIDER_NONPRESENT_DATA);
        } catch (SQLException ex) {
            throw new TmsException("entity not updated", ErrorCodes.REPO_PROVIDER_ISSUES);
        }
    }
}
