package com.agnux.haul.repository;

import com.agnux.haul.repository.model.Agreement;

import java.sql.*;

import java.util.UUID;

class BasicRepoAgreementHelper extends BasicRepoCommonHelper {

    public static void block(Connection conn, UUID agreementId) throws SQLException {
        String sql = "UPDATE agreements SET blocked = true WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, agreementId);
            stmt.executeUpdate();
        }
    }
}
