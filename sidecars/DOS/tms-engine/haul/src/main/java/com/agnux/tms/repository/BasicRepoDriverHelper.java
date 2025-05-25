package com.agnux.tms.repository;

import com.agnux.tms.errors.TmsException;
import com.agnux.tms.repository.model.Driver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

class BasicRepoDriverHelper extends BasicRepoCommonHelper {

    public static final String ENTITY_NAME = "driver";
    public static final String ENTITY_TABLE = "drivers";

    public static Optional<Driver> fetchById(Connection conn, UUID driverId) throws SQLException {
        String sql = String.format(FETCH_BY_ID_SQL_QUERY, ENTITY_TABLE);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, driverId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }

                return Optional.of(fromResultSet(rs));
            }
        }
    }

    public static UUID update(Connection conn, boolean debugMode, Driver d) throws SQLException {

        if (debugMode) {
            verifyPgFunctionExists(conn, "alter_driver");
        }

        String sql = "SELECT * FROM alter_driver(?::UUID, ?::UUID, ?::VARCHAR, ?::VARCHAR, ?::VARCHAR, ?::VARCHAR) AS (driver_id UUID, message TEXT)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Set the input parameters
            if (d.getId().isPresent()) {
                stmt.setObject(1, d.getId().get());
            } else {
                stmt.setNull(1, Types.OTHER); // _driver_id
            }

            stmt.setObject(2, d.getTenantId());           // _tenant_id
            stmt.setString(3, d.getName());               // _name
            stmt.setString(4, d.getFirstSurname());       // _first_surname
            stmt.setString(5, d.getSecondSurname());      // _second_surname
            stmt.setString(6, d.getLicenseNumber());      // _license_number

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    UUID returnedDriverId = (UUID) rs.getObject(1);
                    String returnedMessage = rs.getString(2);

                    if (returnedDriverId != null) {
                        return returnedDriverId;
                    }

                    throw new RuntimeException("Driver update failed: " + returnedMessage);

                } else {
                    throw new RuntimeException("Function returned no result");
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("DB error during driver update", ex);
        }
    }

    public static void block(Connection conn, UUID id) throws TmsException {
        blockAt(conn, ENTITY_TABLE, id);
    }

    public static PaginationSegment<Driver> list(Connection conn, Map<String, String> searchParams, Map<String, String> pageParams) throws TmsException {

        return new Lister<>(
                ENTITY_TABLE,
                Set.of("id", "tenant_id", "name", "first_surname", "second_surname", "license_number"),
                Arrays.asList("id", "tenant_id", "name", "first_surname", "second_surname", "license_number"),
                BasicRepoDriverHelper::fromResultSet
        ).list(conn, searchParams, pageParams);
    }

    public static Driver fromResultSet(ResultSet rs) throws SQLException {
        UUID id = UUID.fromString(rs.getString("id"));
        UUID tenantId = UUID.fromString(rs.getString("tenant_id"));
        String name = rs.getString("name");
        String firstSurname = rs.getString("first_surname");
        String secondSurname = rs.getString("second_surname");
        String licenseNumber = rs.getString("license_number");
        return new Driver(id, tenantId, name, firstSurname, secondSurname, licenseNumber);
    }
}
