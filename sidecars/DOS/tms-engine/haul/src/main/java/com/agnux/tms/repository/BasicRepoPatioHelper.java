package com.agnux.tms.repository;

import com.agnux.tms.errors.TmsException;
import com.agnux.tms.repository.model.Patio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Optional;
import java.util.UUID;

class BasicRepoPatioHelper extends BasicRepoCommonHelper {

    public static final String ENTITY_NAME = "patio";
    public static final String ENTITY_TABLE = "patios";

    public static Optional<Patio> fetchById(Connection conn, UUID patioId) throws SQLException {
        String sql = String.format("SELECT * FROM %s WHERE not blocked AND id = ?", ENTITY_TABLE);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, patioId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }

                UUID tenantId = UUID.fromString(rs.getString("tenant_id"));
                String name = rs.getString("name");
                double latitudeLocation = rs.getDouble("latitude_location");
                double longitudeLocation = rs.getDouble("longitude_location");

                Patio patio = new Patio(patioId, tenantId, name, latitudeLocation, longitudeLocation);
                return Optional.of(patio);
            }
        }
    }

    public static UUID update(Connection conn, boolean debugMode, Patio p) throws SQLException {

        if (debugMode) {
            verifyPgFunctionExists(conn, "alter_patio");
        }

        String sql = "SELECT * FROM alter_patio(?::UUID, ?::UUID, ?::VARCHAR, ?::DOUBLE PRECISION, ?::DOUBLE PRECISION) AS (patio_id UUID, message TEXT)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (p.getId().isPresent()) {
                stmt.setObject(1, p.getId().get());
            } else {
                stmt.setNull(1, Types.OTHER); // _patio_id
            }

            stmt.setObject(2, p.getTenantId());                   // _tenant_id
            stmt.setString(3, p.getName());                       // _name
            stmt.setDouble(4, p.getLatitudeLocation());           // _latitude_location
            stmt.setDouble(5, p.getLongitudeLocation());          // _longitude_location

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    UUID returnedPatioId = (UUID) rs.getObject(1);
                    String returnedMessage = rs.getString(2);

                    if (returnedPatioId != null) {
                        return returnedPatioId;
                    }

                    throw new RuntimeException("Patio update failed: " + returnedMessage);
                } else {
                    throw new RuntimeException("Function returned no result");
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("DB error during patio update", ex);
        }
    }

    public static void block(Connection conn, UUID id) throws TmsException {
        blockAt(conn, ENTITY_TABLE, id);
    }
}
