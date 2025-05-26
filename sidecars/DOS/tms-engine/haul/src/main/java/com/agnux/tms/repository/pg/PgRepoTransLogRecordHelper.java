package com.agnux.tms.repository.pg;

import com.agnux.tms.repository.model.DistUnit;
import com.agnux.tms.repository.model.TransLogRecord;

import java.math.BigDecimal;
import java.sql.*;
import java.util.Optional;
import java.util.UUID;

class PgRepoTransLogRecordHelper extends PgRepoCommonHelper {

    public static final String ENTITY_NAME = "trans_log_record";
    public static final String ENTITY_TABLE = "trans_log_records";

    public static Optional<TransLogRecord> fetchById(Connection conn, UUID recordId) throws SQLException {
        String sql = String.format("SELECT * FROM %s WHERE id = ?", ENTITY_TABLE);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, recordId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }

                UUID tenantId = UUID.fromString(rs.getString("tenant_id"));
                String distUnitStr = rs.getString("dist_unit");
                BigDecimal distScalar = rs.getBigDecimal("dist_scalar");
                BigDecimal fuelConsumption = rs.getBigDecimal("fuel_consumption");
                UUID cargoAssignmentId = rs.getObject("cargo_assignment_id") != null
                        ? UUID.fromString(rs.getString("cargo_assignment_id"))
                        : null;

                DistUnit distUnit = DistUnit.valueOf(distUnitStr);

                TransLogRecord record = new TransLogRecord(recordId, tenantId, distUnit, cargoAssignmentId, distScalar, fuelConsumption);
                return Optional.of(record);
            }
        }
    }

    public static UUID update(Connection conn, boolean debugMode, TransLogRecord record) throws SQLException {

        if (debugMode) {
            verifyPgFunctionExists(conn, "alter_trans_log_record");
        }

        String sql = "SELECT * FROM alter_trans_log_record(?::UUID, ?::UUID, ?::UUID, ?::VARCHAR, ?::NUMERIC, ?::NUMERIC) AS (trans_log_record_id UUID, message TEXT)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            // _id
            if (record.getId().isPresent()) {
                stmt.setObject(1, record.getId().get());
            } else {
                stmt.setNull(1, Types.OTHER);
            }

            stmt.setObject(2, record.getTenantId()); // _tenant_id
            stmt.setObject(3, record.getCargoAssignmentId()); // _cargo_assignment_id
            stmt.setString(4, record.getDistUnit().toString()); // _dist_unit
            stmt.setBigDecimal(5, record.getDistScalar()); // _dist_scalar

            if (record.getFuelConsumption() != null) {
                stmt.setBigDecimal(6, record.getFuelConsumption()); // _fuel_consumption
            } else {
                stmt.setNull(6, Types.NUMERIC);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    UUID returnedId = (UUID) rs.getObject(1);
                    String returnedMessage = rs.getString(2);

                    if (returnedId != null) {
                        return returnedId;
                    }

                    throw new RuntimeException("TransLogRecord update failed: " + returnedMessage);
                } else {
                    throw new RuntimeException("Function returned no result");
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("DB error during TransLogRecord update", ex);
        }
    }
}
