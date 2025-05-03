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

    public static UUID updateVehicle(Connection conn, Vehicle v) {
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

    /*
    public static UUID updateVehicle(DataSource dataSource, Vehicle v) {

        String sql = "{? = call alter_vehicle(?::UUID, ?::UUID, ?::VARCHAR, ?::VARCHAR, ?::VARCHAR, ?::VARCHAR, ?::NUMERIC)}"; // Added one more '?'

        try (Connection conn = dataSource.getConnection(); CallableStatement proc = conn.prepareCall(sql)) {

            // Register the output parameter (the RECORD being returned)
            proc.registerOutParameter(1, Types.OTHER);

            // Set the input parameters
            if (v.getId() == null) {
                proc.setNull(2, Types.OTHER); // _vehicle_id
            } else {
                proc.setObject(2, v.getId());
            }

            proc.setObject(3, v.getTenantId());         // _tenant_id
            proc.setString(4, v.getNumberPlate());     // _number_plate
            proc.setString(5, v.getVehicleType().toString());    // _vehicle_type
            proc.setString(6, v.getPerfDistUnit().toString());  // _perf_dist_unit
            proc.setString(7, v.getPerfVolUnit().toString());   // _perf_vol_unit
            proc.setBigDecimal(8, v.getPerfScalar());        // _perf_scalar

            proc.execute();

            // Retrieve the returned RECORD
            Object result = proc.getObject(1);
            if (result != null) {
                Object[] resultArray = (Object[]) result;
                UUID returnedVehicleId = (UUID) resultArray[0];
                String returnedMessage = (String) resultArray[1];

                if (returnedVehicleId == null) {
                    throw new RuntimeException("Vehicle update failed: " + returnedMessage);
                } else {
                    return returnedVehicleId;
                }
            } else {
                throw new RuntimeException("Procedure did not return a RECORD");
            }
        } catch (SQLException ex) {
            throw new RuntimeException("DB error during vehicle update", ex);
        }
    }*/

 /*
        public static UUID updateVehicle(DataSource dataSource, Vehicle v) {
        String sql = "{? = call public.alter_vehicle(?::UUID, ?::UUID, ?::VARCHAR, ?::VARCHAR, ?::VARCHAR, ?::VARCHAR, ?::NUMERIC)}";

        try (Connection conn = dataSource.getConnection(); CallableStatement proc = conn.prepareCall(sql)) {

            proc.registerOutParameter(1, Types.OTHER);

            // Set the input parameters
            if (v.getId() == null) {
                proc.setNull(2, Types.OTHER); // _vehicle_id
            } else {
                proc.setObject(2, v.getId());
            }

            proc.setObject(3, v.getTenantId());
            proc.setString(4, v.getNumberPlate());
            proc.setString(5, v.getVehicleType().toString());
            proc.setString(6, v.getPerfDistUnit().toString());
            proc.setString(7, v.getPerfVolUnit().toString());
            proc.setBigDecimal(8, v.getPerfScalar());

            proc.execute();

            Object result = proc.getObject(1);
            if (result instanceof PGobject pgObject) {
                String value = pgObject.getValue(); // E.g. "(uuid,'')"

                if (value != null && value.startsWith("(") && value.endsWith(")")) {
                    String[] parts = value.substring(1, value.length() - 1).split(",", 2);
                    UUID returnedId = UUID.fromString(parts[0].replace("\"", "").trim());
                    String message = parts[1].replace("\"", "").trim();

                    if (returnedId == null) {
                        throw new RuntimeException("Vehicle update failed: " + message);
                    }

                    return returnedId;
                } else {
                    throw new RuntimeException("Unexpected format in returned RECORD: " + value);
                }
            } else {
                throw new RuntimeException("Procedure did not return PGobject as expected");
            }
        } catch (SQLException ex) {
            throw new RuntimeException("DB error during vehicle update", ex);
        }
    }
    public static UUID updateVehicle(DataSource dataSource, Vehicle v) {

        String sql = "{? = call public.alter_vehicle(?, ?, ?, ?, ?, ?)}";

        try (Connection conn = dataSource.getConnection(); CallableStatement proc = conn.prepareCall(sql)) {

            // Register the output parameter (the RECORD being returned)
            proc.registerOutParameter(1, Types.OTHER);

            // Set the input parameters
            if (v.getId() == null) {
                proc.setNull(2, Types.OTHER);
            } else {
                proc.setObject(2, v.getId());
            }

            proc.setObject(3, v.getTenantId());                 // UUID
            proc.setString(4, v.getNumberPlate());              // VARCHAR
            proc.setString(5, v.getVehicleType().toString());   // VARCHAR
            proc.setString(6, v.getPerfDistUnit().toString());  // VARCHAR
            proc.setString(7, v.getPerfVolUnit().toString());   // VARCHAR
            proc.setBigDecimal(8, v.getPerfScalar());           // NUMERIC

            proc.execute();

            // Retrieve the returned RECORD
            Object result = proc.getObject(1);
            if (result != null) {
                Object[] resultArray = (Object[]) result;
                UUID returnedVehicleId = (UUID) resultArray[0];
                String returnedMessage = (String) resultArray[1];

                if (returnedVehicleId == null) {
                    throw new RuntimeException("Vehicle update failed: " + returnedMessage);
                } else {
                    return returnedVehicleId;
                }
            } else {
                throw new RuntimeException("Procedure did not return a RECORD");
            }
        } catch (SQLException ex) {
            throw new RuntimeException("DB error during vehicle update", ex);
        }
    }*/

 /*    
    public static UUID updateVehicle(DataSource dataSource, Vehicle v) {
        String sql = "SELECT * FROM public.alter_vehicle(?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            //stmt.setObject(1, v.getId());                       // UUID or null
            //stmt.setNull(1, Types.OTHER);
            stmt.setNull(1, Types.VARCHAR);
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
     */
    @Override
    public Vehicle createVehicle() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
