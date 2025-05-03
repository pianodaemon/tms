package com.agnux.haul.repository;

import com.agnux.haul.repository.model.Agreement;
import com.agnux.haul.repository.model.CargoAssignment;
import com.agnux.haul.repository.model.Vehicle;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import javax.sql.DataSource;

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

    public static UUID updateVehicle(DataSource dataSource, Vehicle v) {
        String sql = "SELECT * FROM public.alter_vehicle(?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, v.getId());                       // UUID or null
            stmt.setObject(2, v.getTenantId());                 // UUID
            stmt.setString(3, v.getNumberPlate());              // VARCHAR
            stmt.setString(4, v.getVehicleType().toString());   // VARCHAR
            stmt.setString(5, v.getPerfDistUnit().toString());  // VARCHAR
            stmt.setString(6, v.getPerfVolUnit().toString());   // VARCHAR
            stmt.setBigDecimal(7, v.getPerfScalar());           // NUMERIC

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                UUID createdId = (UUID) rs.getObject(1);
                String errorMsg = rs.getString(2);

                if (createdId == null) {
                    throw new RuntimeException("Vehicle update failed: " + errorMsg);
                }

                return createdId;
            }

        } catch (SQLException ex) {
            throw new RuntimeException("DB error during vehicle update", ex);
        }

        throw new RuntimeException("Unexpected error during vehicle update");
    }

    @Override
    public Vehicle createVehicle() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
