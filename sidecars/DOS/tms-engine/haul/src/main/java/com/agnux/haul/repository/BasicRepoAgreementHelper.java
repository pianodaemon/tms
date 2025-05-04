package com.agnux.haul.repository;

import com.agnux.haul.repository.model.Agreement;
import com.agnux.haul.repository.model.DistUnit;

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
                ?::UUID, ?::UUID, ?::VARCHAR, ?::DOUBLE PRECISION, ?::DOUBLE PRECISION,
                ?::DOUBLE PRECISION, ?::DOUBLE PRECISION, ?::VARCHAR, ?::NUMERIC
            ) AS (agreement_id UUID, message TEXT)
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (a.getId().isPresent()) {
                stmt.setObject(1, a.getId().get());               // _agreement_id
            } else {
                stmt.setNull(1, Types.OTHER);
            }

            stmt.setObject(2, a.getTenantId());                   // _tenant_id
            stmt.setObject(3, a.getCustomerId());                 // _customer_id
            stmt.setDouble(4, a.getLatitudeOrigin());             // _latitude_origin
            stmt.setDouble(5, a.getLongitudeOrigin());            // _longitude_origin
            stmt.setDouble(6, a.getLatitudeDestiny());            // _latitude_destiny
            stmt.setDouble(7, a.getLongitudeDestiny());           // _longitude_destiny
            stmt.setString(8, a.getDistUnit().name());            // _dist_unit
            stmt.setBigDecimal(9, a.getDistScalar());             // _dist_scalar

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    UUID returnedId = (UUID) rs.getObject(1);
                    String message = rs.getString(2);

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
