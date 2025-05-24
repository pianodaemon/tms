package com.agnux.tms.repository;

import com.agnux.tms.errors.ErrorCodes;
import com.agnux.tms.errors.TmsException;
import com.agnux.tms.repository.model.CargoAssignment;

import java.sql.*;
import java.util.Optional;
import java.util.UUID;

class BasicRepoCargoAssignmentHelper extends BasicRepoCommonHelper {

    public static Optional<CargoAssignment> fetchById(Connection conn, UUID assignmentId) throws SQLException {
        String sql = "SELECT * FROM cargo_assignments WHERE not blocked AND id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, assignmentId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }

                UUID tenantId = UUID.fromString(rs.getString("tenant_id"));
                UUID driverId = (UUID) rs.getObject("driver_id");
                UUID vehicleId = (UUID) rs.getObject("vehicle_id");
                double latitude = rs.getDouble("latitude_location");
                double longitude = rs.getDouble("longitude_location");

                CargoAssignment assignment = new CargoAssignment(assignmentId, tenantId, driverId, vehicleId, latitude, longitude);

                return Optional.of(assignment);
            }
        }
    }

    public static UUID update(Connection conn, boolean debugMode, CargoAssignment ca) throws SQLException {

        if (debugMode) {
            verifyPgFunctionExists(conn, "alter_cargo_assignment");
        }

        String sql = "SELECT * FROM alter_cargo_assignment(?::UUID, ?::UUID, ?::UUID, ?::UUID, ?::DOUBLE PRECISION, ?::DOUBLE PRECISION) "
                + "AS (assignment_id UUID, message TEXT)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (ca.getId().isPresent()) {
                stmt.setObject(1, ca.getId().get()); // _assignment_id
            } else {
                stmt.setNull(1, Types.OTHER);
            }

            stmt.setObject(2, ca.getTenantId());                            // _tenant_id
            stmt.setObject(3, ca.getDriverId());                            // _driver_id
            stmt.setObject(4, ca.getVehicleId());                           // _vehicle_id
            stmt.setDouble(5, ca.getLatitudeLocation());                    // _latitude
            stmt.setDouble(6, ca.getLongitudeLocation());                   // _longitude

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    UUID returnedId = (UUID) rs.getObject("assignment_id");
                    String msg = rs.getString("message");

                    if (returnedId != null) {
                        return returnedId;
                    }

                    throw new RuntimeException("CargoAssignment update failed: " + msg);
                } else {
                    throw new RuntimeException("Function returned no result");
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("DB error during cargo assignment update", ex);
        }
    }

    public static void block(Connection conn, UUID assignmentId) throws TmsException {

        String sql = "UPDATE cargo_assignments SET blocked = true WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, assignmentId);
            int updates = stmt.executeUpdate();
            if (updates == 1) {
                return;
            }
            throw new TmsException("assignment not deleted", ErrorCodes.REPO_PROVIDER_NONPRESENT_DATA);
        } catch (SQLException ex) {
            throw new TmsException("assignment not deleted", ErrorCodes.REPO_PROVIDER_ISSUES);
        }
    }
}
