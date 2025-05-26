package com.agnux.tms.repository.pg;

import com.agnux.tms.errors.ErrorCodes;
import com.agnux.tms.errors.TmsException;
import com.agnux.tms.repository.PaginationSegment;
import com.agnux.tms.repository.model.Customer;

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

class PgRepoCustomerHelper extends PgRepoCommonHelper {

    public static final String ENTITY_NAME = "customer";
    public static final String ENTITY_TABLE = "customers";

    public static Optional<Customer> fetchById(Connection conn, UUID customerId) throws SQLException {
        String sql = String.format(FETCH_BY_ID_SQL_QUERY, ENTITY_TABLE);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, customerId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }

                return Optional.of(fromResultSet(rs));
            }
        }
    }

    public static UUID update(Connection conn, boolean debugMode, Customer c) throws SQLException {

        if (debugMode) {
            verifyPgFunctionExists(conn, "alter_customer");
        }

        String sql = "SELECT * FROM alter_customer(?::UUID, ?::UUID, ?::VARCHAR) AS (customer_id UUID, message TEXT)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Set the input parameters
            if (c.getId().isPresent()) {
                stmt.setObject(1, c.getId().get());
            } else {
                stmt.setNull(1, Types.OTHER); // _customer_id
            }

            stmt.setObject(2, c.getTenantId());           // _tenant_id
            stmt.setString(3, c.getName());               // _name

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    UUID returnedCustomerId = (UUID) rs.getObject(1);
                    String returnedMessage = rs.getString(2);

                    if (returnedCustomerId != null) {
                        return returnedCustomerId;
                    }

                    throw new RuntimeException("Customer update failed: " + returnedMessage);

                } else {
                    throw new RuntimeException("Function returned no result");
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("DB error during customer update", ex);
        }
    }

    public static void block(Connection conn, UUID id) throws TmsException {
        blockAt(conn, ENTITY_TABLE, id);
    }

    public static PaginationSegment<Customer> list(Connection conn, Map<String, String> searchParams, Map<String, String> pageParams) throws TmsException {

        return new PgLister<>(
                ENTITY_TABLE,
                Set.of("id", "tenant_id", "name"),
                Arrays.asList("id", "tenant_id", "name"),
                PgRepoCustomerHelper::fromResultSet
        ).list(conn, searchParams, pageParams);
    }

    public static Customer fromResultSet(ResultSet rs) throws SQLException {
        UUID id = UUID.fromString(rs.getString("id"));
        UUID tenantId = UUID.fromString(rs.getString("tenant_id"));
        String name = rs.getString("name");
        return new Customer(id, tenantId, name);
    }
}
