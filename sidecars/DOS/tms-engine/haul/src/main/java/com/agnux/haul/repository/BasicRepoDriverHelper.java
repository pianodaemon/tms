package com.agnux.haul.repository;

import com.agnux.haul.repository.model.Driver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Optional;
import java.util.UUID;

class BasicRepoDriverHelper extends BasicRepoCommonHelper {

    public static Optional<Driver> fetchById(Connection conn, UUID driverId) throws SQLException {
        String sql = "SELECT * FROM drivers WHERE NOT blocked AND id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, driverId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }

                UUID tenantId = UUID.fromString(rs.getString("tenant_id"));
                String name = rs.getString("name");
                String licenseNumber = rs.getString("license_number");

                Driver driver = new Driver(driverId, tenantId, name, licenseNumber);

                return Optional.of(driver);
            }
        }
    }

    public static UUID update(Connection conn, boolean debugMode, Driver d) throws SQLException {

        if (debugMode) {
            verifyPgFunctionExists(conn, "alter_driver");
        }

        String sql = "SELECT * FROM alter_driver(?::UUID, ?::UUID, ?::VARCHAR, ?::VARCHAR) AS (driver_id UUID, message TEXT)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Set the input parameters
            if (d.getId().isPresent()) {
                stmt.setObject(1, d.getId().get());
            } else {
                stmt.setNull(1, Types.OTHER); // _driver_id
            }

            stmt.setObject(2, d.getTenantId());           // _tenant_id
            stmt.setString(3, d.getName());               // _name
            stmt.setString(4, d.getLicenseNumber());      // _license_number

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    UUID returnedDriverId = (UUID) rs.getObject(1);
                    String returnedMessage = rs.getString(2);

                    if (returnedDriverId != null) {
                        return returnedDriverId;
                    }

                    throw new RuntimeException("Driver update failed: " + returnedMessage);

                } else {
                    throw new RuntimeException("Function returned no result");
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("DB error during driver update", ex);
        }
    }

    public static void block(Connection conn, UUID driverId) throws SQLException {
        String sql = "UPDATE drivers SET blocked = true WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, driverId);
            stmt.executeUpdate();
        }
    }
}
