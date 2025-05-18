package com.agnux.tms.repository;

import com.agnux.tms.repository.model.DistUnit;
import com.agnux.tms.repository.model.Vehicle;
import com.agnux.tms.repository.model.VehicleType;
import com.agnux.tms.repository.model.VolUnit;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Optional;
import java.util.UUID;

class BasicRepoVehicleHelper extends BasicRepoCommonHelper {

    public static Optional<Vehicle> fetchById(Connection conn, UUID vehicleId) throws SQLException {
        String sql = "SELECT * FROM vehicles WHERE not blocked AND id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, vehicleId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }

                UUID tenantId = UUID.fromString(rs.getString("tenant_id"));
                String numberPlate = rs.getString("number_plate");
                String numberSerial = rs.getString("number_serial");
                VehicleType vehicleType = VehicleType.valueOf(rs.getString("vehicle_type"));
                Integer vehicleYear = rs.getInt("vehicle_year");
                String federalConf = rs.getString("federal_conf");
                String distUnitStr = rs.getString("perf_dist_unit");
                String volUnitStr = rs.getString("perf_vol_unit");

                Vehicle vehicle = new Vehicle(vehicleId, tenantId, numberPlate, numberSerial,
                        vehicleType, vehicleYear, federalConf, DistUnit.valueOf(distUnitStr),
                        VolUnit.valueOf(volUnitStr));

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

        String sql = "SELECT * FROM alter_vehicle(?::UUID, ?::UUID, ?::VARCHAR, ?::VARCHAR, ?::VARCHAR, ?::INT, ?::VARCHAR, ?::VARCHAR, ?::VARCHAR, ?::NUMERIC) AS (vehicle_id UUID, message TEXT)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Set the input parameters
            if (v.getId().isPresent()) {
                stmt.setObject(1, v.getId().get());
            } else {
                stmt.setNull(1, Types.OTHER); // _vehicle_id
            }

            stmt.setObject(2, v.getTenantId());                   // _tenant_id
            stmt.setString(3, v.getNumberPlate());                // _number_plate
            stmt.setString(4, v.getNumberSerial());               // _number_serial
            stmt.setString(5, v.getVehicleType().toString());     // _vehicle_type
            stmt.setInt(6, v.getVehicleYear());                   // _vehicle_year
            stmt.setString(7, v.getFederalConf());                // _federal_conf
            stmt.setString(8, v.getPerfDistUnit().toString());    // _perf_dist_unit
            stmt.setString(9, v.getPerfVolUnit().toString());     // _perf_vol_unit
            stmt.setBigDecimal(10, v.getPerfScalar());             // _perf_scalar

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

    public static void block(Connection conn, UUID vehicleId) throws SQLException {
        String sql = "UPDATE vehicles SET blocked = true WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, vehicleId);
            stmt.executeUpdate();
        }
    }
}
