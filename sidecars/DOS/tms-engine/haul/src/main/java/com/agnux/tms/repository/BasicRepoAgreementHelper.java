package com.agnux.tms.repository;

import com.agnux.tms.repository.model.Agreement;
import com.agnux.tms.repository.model.DistUnit;

import java.math.BigDecimal;
import java.sql.*;
import java.util.Optional;
import java.util.UUID;

class BasicRepoAgreementHelper extends BasicRepoCommonHelper {

    public static Optional<Agreement> fetchById(Connection conn, UUID agreementId) throws SQLException {
        String sql = "SELECT * FROM agreements WHERE NOT blocked AND id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, agreementId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }

                UUID tenantId = UUID.fromString(rs.getString("tenant_id"));
                UUID customerId = UUID.fromString(rs.getString("customer_id"));
                String receptor = rs.getString("receptor");
                double latitudeOrigin = rs.getDouble("latitude_origin");
                double longitudeOrigin = rs.getDouble("longitude_origin");
                double latitudeDestiny = rs.getDouble("latitude_destiny");
                double longitudeDestiny = rs.getDouble("longitude_destiny");
                DistUnit distUnit = DistUnit.valueOf(rs.getString("dist_unit"));
                BigDecimal distScalar = rs.getBigDecimal("dist_scalar");

                Agreement agreement = new Agreement(
                        agreementId,
                        tenantId,
                        customerId,
                        receptor,
                        latitudeOrigin,
                        longitudeOrigin,
                        latitudeDestiny,
                        longitudeDestiny,
                        distUnit,
                        distScalar
                );

                return Optional.of(agreement);
            }
        }
    }

    public static UUID update(Connection conn, boolean debugMode, Agreement a) throws SQLException {
        if (debugMode) {
            verifyPgFunctionExists(conn, "alter_agreement");
        }

        String sql = """
            SELECT * FROM alter_agreement(
                ?::UUID, ?::UUID, ?::UUID, ?::VARCHAR,
                ?::DOUBLE PRECISION, ?::DOUBLE PRECISION,
                ?::DOUBLE PRECISION, ?::DOUBLE PRECISION,
                ?::VARCHAR, ?::NUMERIC
            ) AS (agreement_id UUID, message TEXT)
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (a.getId().isPresent()) {
                stmt.setObject(1, a.getId().get());
            } else {
                stmt.setNull(1, Types.OTHER);
            }

            stmt.setObject(2, a.getTenantId());
            stmt.setObject(3, a.getCustomerId());
            stmt.setString(4, a.getReceptor());
            stmt.setDouble(5, a.getLatitudeOrigin());
            stmt.setDouble(6, a.getLongitudeOrigin());
            stmt.setDouble(7, a.getLatitudeDestiny());
            stmt.setDouble(8, a.getLongitudeDestiny());
            stmt.setString(9, a.getDistUnit().name());
            stmt.setBigDecimal(10, a.getDistScalar());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    UUID returnedId = rs.getObject("agreement_id", UUID.class);
                    String message = rs.getString("message");

                    if (returnedId != null) {
                        return returnedId;
                    }
                    throw new RuntimeException("Agreement update failed: " + message);
                } else {
                    throw new RuntimeException("Function returned no result");
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("DB error during agreement update", ex);
        }
    }

    public static void block(Connection conn, UUID agreementId) throws SQLException {
        String sql = "UPDATE agreements SET blocked = true WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, agreementId);
            stmt.executeUpdate();
        }
    }
}
