package com.agnux.tms.repository.pg;

import com.agnux.tms.errors.TmsException;
import com.agnux.tms.repository.PaginationSegment;
import com.agnux.tms.repository.model.Vehicle;
import com.agnux.tms.reference.quantitative.DistUnit;
import com.agnux.tms.reference.qualitative.VehicleColor;
import com.agnux.tms.reference.qualitative.VehicleType;
import com.agnux.tms.reference.quantitative.VolUnit;

import java.math.BigDecimal;
import java.sql.*;
import java.sql.Date;
import java.util.*;

class PgRepoVehicleHelper extends PgRepoCommonHelper {

    public static final String ENTITY_NAME = "vehicule";
    public static final String ENTITY_TABLE = "vehicles";

    public static Optional<Vehicle> fetchById(Connection conn, UUID vehicleId) throws SQLException {
        String sql = String.format(FETCH_BY_ID_SQL_QUERY, ENTITY_TABLE);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, vehicleId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }

                return Optional.of(fromResultSet(rs));
            }
        }
    }

    public static UUID update(Connection conn, boolean debugMode, Vehicle v) throws SQLException {

        if (debugMode) {
            verifyPgFunctionExists(conn, "alter_vehicle");
        }

        String sql = "SELECT * FROM alter_vehicle(?::UUID, ?::UUID, ?::VARCHAR, ?::DATE, ?::VARCHAR, ?::SMALLINT, ?::DATE, ?::VARCHAR, ?::VARCHAR, ?::INT, ?::VARCHAR, ?::VARCHAR, ?::VARCHAR, ?::NUMERIC) AS (vehicle_id UUID, message TEXT)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Set the input parameters
            if (v.getId().isPresent()) {
                stmt.setObject(1, v.getId().get());
            } else {
                stmt.setNull(1, Types.OTHER); // _vehicle_id
            }

            stmt.setObject(2, v.getTenantId());                   // _tenant_id
            stmt.setString(3, v.getNumberPlate());                // _number_plate

            var expirationDate = new java.sql.Date(v.getNumberPlateExpiration().getTime());
            stmt.setDate(4, expirationDate);                      // _number_plate_expiration

            stmt.setString(5, v.getNumberSerial());               // _number_serial
            stmt.setInt(6, v.getNumberOfAxis());                  // _number_axis

            var insuranceExpirationDate = new java.sql.Date(v.getInsuranceExpiration().getTime());
            stmt.setDate(7, insuranceExpirationDate);                      // _insurance_expiration

            stmt.setString(8, v.getVehicleType().toString());     // _vehicle_type
            stmt.setString(9, v.getVehicleColor().toString());     // _vehicle_color
            stmt.setInt(10, v.getVehicleYear());                   // _vehicle_year
            stmt.setString(11, v.getFederalConf());                // _federal_conf
            stmt.setString(12, v.getPerfDistUnit().toString());    // _perf_dist_unit
            stmt.setString(13, v.getPerfVolUnit().toString());     // _perf_vol_unit
            stmt.setBigDecimal(14, v.getPerfScalar());             // _perf_scalar

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

    public static void block(Connection conn, UUID id) throws TmsException {
        blockAt(conn, ENTITY_TABLE, id);
    }

    public static PaginationSegment<Vehicle> list(Connection conn, Map<String, String> searchParams, Map<String, String> pageParams) throws TmsException {
        return new PgLister<>(
                ENTITY_TABLE,
                Set.of("id", "tenant_id", "number_plate", "number_serial", "vehicle_type", "vehicle_color", "federal_conf", "perf_dist_unit", "perf_vol_unit"),
                List.of("*"),
                PgRepoVehicleHelper::fromResultSet
        ).list(conn, searchParams, pageParams);
    }

    public static Vehicle fromResultSet(ResultSet rs) throws SQLException {

        UUID vehicleId = UUID.fromString(rs.getString("id"));
        UUID tenantId = UUID.fromString(rs.getString("tenant_id"));
        String numberPlate = rs.getString("number_plate");
        Date expirationDate = rs.getDate("number_plate_expiration");
        String numberSerial = rs.getString("number_serial");
        VehicleType vehicleType = VehicleType.valueOf(rs.getString("vehicle_type"));
        VehicleColor vehicleColor = VehicleColor.valueOf(rs.getString("vehicle_color"));
        int vehicleYear = rs.getInt("vehicle_year");
        String federalConf = rs.getString("federal_conf");
        String distUnitStr = rs.getString("perf_dist_unit");
        String volUnitStr = rs.getString("perf_vol_unit");
        BigDecimal scalar = rs.getBigDecimal("perf_scalar");
        int numberOfAxis = rs.getInt("number_axis");
        Date insuranceExpirationDate = rs.getDate("insurance_expiration");

        return new Vehicle(vehicleId, tenantId, numberPlate, expirationDate, numberSerial, numberOfAxis,
                vehicleType, vehicleColor, vehicleYear, insuranceExpirationDate, federalConf, DistUnit.valueOf(distUnitStr),
                VolUnit.valueOf(volUnitStr), scalar);
    }
}
