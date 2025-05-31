package com.agnux.tms.repository.pg;

import com.agnux.tms.errors.TmsException;
import com.agnux.tms.reference.qualitative.BoxBrand;
import com.agnux.tms.reference.qualitative.BoxType;
import com.agnux.tms.repository.PaginationSegment;
import com.agnux.tms.repository.model.Box;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

class PgRepoBoxHelper extends PgRepoCommonHelper {

    public static final String ENTITY_NAME = "box";
    public static final String ENTITY_TABLE = "boxes";

    public static Optional<Box> fetchById(Connection conn, UUID boxId) throws SQLException {
        String sql = String.format(FETCH_BY_ID_SQL_QUERY, ENTITY_TABLE);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, boxId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }

                return Optional.of(fromResultSet(rs));
            }
        }
    }

    public static UUID update(Connection conn, boolean debugMode, Box c) throws SQLException {

        if (debugMode) {
            verifyPgFunctionExists(conn, "alter_box");
        }

        String sql = "SELECT * FROM alter_box(?::UUID, ?::UUID, ?::VARCHAR, ?::VARCHAR, ?::VARCHAR, ?::VARCHAR, ?::VARCHAR, ?::DATE, ?::INT, ?::BOOLEAN) AS (box_id UUID, message TEXT)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Set the input parameters
            if (c.getId().isPresent()) {
                stmt.setObject(1, c.getId().get());
            } else {
                stmt.setNull(1, Types.OTHER); // _box_id
            }

            stmt.setObject(2, c.getTenantId());           // _tenant_id
            stmt.setString(3, c.getName());               // _name
            stmt.setString(4, c.getBoxType().toString()); // _box_type
            stmt.setString(5, c.getBrand().toString());   // _box_brand
            stmt.setString(6, c.getNumberSerial());       // _number_serial            
            stmt.setString(7, c.getNumberPlate());        // _number_plate

            var expirationDate = new java.sql.Date(c.getNumberPlateExpiration().getTime());
            stmt.setDate(8, expirationDate);              // _number_plate_expiration

            stmt.setInt(9, c.getBoxYear());               // _box_year
            stmt.setBoolean(10, c.isLease());             // _lease

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    UUID returnedBoxId = (UUID) rs.getObject(1);
                    String returnedMessage = rs.getString(2);

                    if (returnedBoxId != null) {
                        return returnedBoxId;
                    }

                    throw new RuntimeException("Box update failed: " + returnedMessage);

                } else {
                    throw new RuntimeException("Function returned no result");
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("DB error during box update", ex);
        }
    }

    public static void block(Connection conn, UUID id) throws TmsException {
        blockAt(conn, ENTITY_TABLE, id);
    }

    public static PaginationSegment<Box> list(Connection conn, Map<String, String> searchParams, Map<String, String> pageParams) throws TmsException {

        return new PgLister<>(
                ENTITY_TABLE,
                Set.of("id", "tenant_id", "name"),
                List.of("*"),
                PgRepoBoxHelper::fromResultSet
        ).list(conn, searchParams, pageParams);
    }

    public static Box fromResultSet(ResultSet rs) throws SQLException {
        UUID id = UUID.fromString(rs.getString("id"));
        UUID tenantId = UUID.fromString(rs.getString("tenant_id"));
        String name = rs.getString("name");
        String serial = rs.getString("number_serial");
        String numberPlate = rs.getString("number_plate");
        Date expirationDate = rs.getDate("number_plate_expiration");
        BoxBrand brand = BoxBrand.valueOf(rs.getString("box_brand"));
        BoxType type = BoxType.valueOf(rs.getString("box_type"));
        int numberOfAxis = rs.getInt("number_axis");
        return new Box(id, tenantId, name, type, brand, numberOfAxis, serial, numberPlate, expirationDate, rs.getInt("box_year"), rs.getBoolean("lease"));
    }
}
