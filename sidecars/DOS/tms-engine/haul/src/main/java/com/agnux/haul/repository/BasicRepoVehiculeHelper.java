package com.agnux.haul.repository;

import com.agnux.haul.repository.model.DistUnit;
import com.agnux.haul.repository.model.Vehicle;
import com.agnux.haul.repository.model.VehicleType;
import com.agnux.haul.repository.model.VolUnit;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Optional;
import java.util.UUID;

class BasicRepoVehiculeHelper extends BasicRepoCommonHelper {

    public static Optional<Vehicle> fetchById(Connection conn, UUID vehicleId) throws SQLException {
        String sql = "SELECT * FROM vehicles WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, vehicleId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }

                UUID tenantId = UUID.fromString(rs.getString("tenant_id"));
                String numberPlate = rs.getString("number_plate");
                VehicleType vehicleType = VehicleType.valueOf(rs.getString("vehicle_type"));

                Vehicle vehicle = new Vehicle(vehicleId, tenantId, numberPlate, vehicleType);

                String distUnitStr = rs.getString("perf_dist_unit");
                if (distUnitStr != null) {
                    vehicle.setPerfDistUnit(DistUnit.valueOf(distUnitStr));
                }

                String volUnitStr = rs.getString("perf_vol_unit");
                if (volUnitStr != null) {
                    vehicle.setPerfVolUnit(VolUnit.valueOf(volUnitStr));
                }

                BigDecimal scalar = rs.getBigDecimal("perf_scalar");
                if (scalar != null) {
                    vehicle.setPerfScalar(scalar);
                }

                return Optional.of(vehicle);
            }
        }
    }

    public static UUID update(Connection conn, boolean debugMode, Vehicle v) throws SQLException {

        if (debugMode) {
            verifyPgFunctionExists(conn, "alter_vehicle");
        }

        String sql = "SELECT * FROM alter_vehicle(?::UUID, ?::UUID, ?::VARCHAR, ?::VARCHAR, ?::VARCHAR, ?::VARCHAR, ?::NUMERIC) AS (vehicle_id UUID, message TEXT)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Set the input parameters
            if (v.getId().isPresent()) {
                stmt.setObject(1, v.getId().get());
            } else {
                stmt.setNull(1, Types.OTHER); // _vehicle_id
            }

            stmt.setObject(2, v.getTenantId());                   // _tenant_id
            stmt.setString(3, v.getNumberPlate());                // _number_plate
            stmt.setString(4, v.getVehicleType().toString());     // _vehicle_type
            stmt.setString(5, v.getPerfDistUnit().toString());    // _perf_dist_unit
            stmt.setString(6, v.getPerfVolUnit().toString());     // _perf_vol_unit
            stmt.setBigDecimal(7, v.getPerfScalar());             // _perf_scalar

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    UUID returnedVehicleId = (UUID) rs.getObject(1);
                    String returnedMessage = rs.getString(2);

                    if (returnedVehicleId != null) {
                        return returnedVehicleId;
                    }

                    throw new RuntimeException("Vehicle update failed: " + returnedMessage);

                } else {
                    throw new RuntimeException("Function returned no result");
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("DB error during vehicle update", ex);
        }
    }
}
