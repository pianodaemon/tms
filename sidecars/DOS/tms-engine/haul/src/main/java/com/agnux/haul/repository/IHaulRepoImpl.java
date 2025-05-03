package com.agnux.haul.repository;

import com.agnux.haul.repository.model.Agreement;
import com.agnux.haul.repository.model.CargoAssignment;
import com.agnux.haul.repository.model.Vehicle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.UUID;

public class IHaulRepoImpl implements IHaulRepo {

    @Override
    public String createCargoAssignment(CargoAssignment t) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Vehicle getAvailableVehicule(UUID vehicleId) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Agreement getAvailableAgreement(UUID agreementId) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    public static UUID updateVehicle(Connection conn, boolean debugMode, Vehicle v) throws SQLException {

        if (debugMode) {
            verifyPgFunctionExists(conn, "alter_vehicle");
        }

        String sql = "SELECT * FROM alter_vehicle(?::UUID, ?::UUID, ?::VARCHAR, ?::VARCHAR, ?::VARCHAR, ?::VARCHAR, ?::NUMERIC) AS (vehicle_id UUID, message TEXT)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Set the input parameters
            if (v.getId() == null) {
                stmt.setNull(1, Types.OTHER); // _vehicle_id
            } else {
                stmt.setObject(1, v.getId());
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

    private static void verifyPgFunctionExists(Connection conn, String functionName) throws SQLException  {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT 1 FROM pg_proc WHERE proname = ?")) {
            stmt.setString(1, functionName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("PostgreSQL function '" + functionName + "' should exist");
                }
            }
        }
    }

    @Override
    public Vehicle createVehicle() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
