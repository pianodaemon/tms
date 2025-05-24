package com.agnux.tms.repository;

import com.agnux.tms.errors.TmsException;
import com.agnux.tms.repository.model.Agreement;
import com.agnux.tms.repository.model.DistUnit;

import java.math.BigDecimal;
import java.sql.*;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

class BasicRepoAgreementHelper extends BasicRepoCommonHelper {

    public static final String ENTITY_NAME = "agreement";
    public static final String ENTITY_TABLE = "agreements";

    public static Optional<Agreement> fetchById(Connection conn, UUID agreementId) throws SQLException {
        String sql = String.format(FETCH_BY_ID_SQL_QUERY, ENTITY_TABLE);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, agreementId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }

                return Optional.of(fromResultSet(rs));
            }
        }
    }

    public static UUID update(Connection conn, boolean debugMode, Agreement a) throws SQLException {
        if (debugMode) {
            verifyPgFunctionExists(conn, "alter_agreement");
        }

        String sql = """
            SELECT * FROM alter_agreement(
                ?::UUID, ?::UUID, ?::UUID, ?::VARCHAR,
                ?::DOUBLE PRECISION, ?::DOUBLE PRECISION,
                ?::DOUBLE PRECISION, ?::DOUBLE PRECISION,
                ?::VARCHAR, ?::NUMERIC
            ) AS (agreement_id UUID, message TEXT)
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (a.getId().isPresent()) {
                stmt.setObject(1, a.getId().get());
            } else {
                stmt.setNull(1, Types.OTHER);
            }

            stmt.setObject(2, a.getTenantId());
            stmt.setObject(3, a.getCustomerId());
            stmt.setString(4, a.getReceiver());
            stmt.setDouble(5, a.getLatitudeOrigin());
            stmt.setDouble(6, a.getLongitudeOrigin());
            stmt.setDouble(7, a.getLatitudeDestiny());
            stmt.setDouble(8, a.getLongitudeDestiny());
            stmt.setString(9, a.getDistUnit().name());
            stmt.setBigDecimal(10, a.getDistScalar());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    UUID returnedId = rs.getObject("agreement_id", UUID.class);
                    String message = rs.getString("message");

                    if (returnedId != null) {
                        return returnedId;
                    }
                    throw new RuntimeException("Agreement update failed: " + message);
                } else {
                    throw new RuntimeException("Function returned no result");
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("DB error during agreement update", ex);
        }
    }

    public static void block(Connection conn, UUID id) throws TmsException {
        blockAt(conn, ENTITY_TABLE, id);
    }

    public static PaginationSegment<Agreement> list(Connection conn, Map<String, String> searchParams, Map<String, String> pageParams) throws TmsException {

        return new Lister<Agreement>(
                ENTITY_TABLE,
                Set.of("id", "tenant_id", "customer_id", "receiver", "dist_unit"),
                Arrays.asList("id", "tenant_id", "customer_id", "receiver", "dist_unit",
                        "dist_scalar", "latitude_origin", "longitude_origin", "latitude_destiny", "longitude_destiny")
        ) {
            @Override
            protected Agreement mapRow(ResultSet rs) throws SQLException {
                return fromResultSet(rs);
            }
        }.list(conn, searchParams, pageParams);
    }

    public static Agreement fromResultSet(ResultSet rs) throws SQLException {
        UUID agreementId = UUID.fromString(rs.getString("id"));
        UUID tenantId = UUID.fromString(rs.getString("tenant_id"));
        UUID customerId = UUID.fromString(rs.getString("customer_id"));
        String receiver = rs.getString("receiver");
        double latitudeOrigin = rs.getDouble("latitude_origin");
        double longitudeOrigin = rs.getDouble("longitude_origin");
        double latitudeDestiny = rs.getDouble("latitude_destiny");
        double longitudeDestiny = rs.getDouble("longitude_destiny");
        DistUnit distUnit = DistUnit.valueOf(rs.getString("dist_unit"));
        BigDecimal distScalar = rs.getBigDecimal("dist_scalar");

        return new Agreement(
                agreementId,
                tenantId,
                customerId,
                receiver,
                latitudeOrigin,
                longitudeOrigin,
                latitudeDestiny,
                longitudeDestiny,
                distUnit,
                distScalar
        );
    }
}
