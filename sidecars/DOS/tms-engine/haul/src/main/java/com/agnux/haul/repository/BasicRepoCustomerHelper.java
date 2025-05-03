package com.agnux.haul.repository;

import com.agnux.haul.repository.model.Customer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Optional;
import java.util.UUID;

class BasicRepoCustomerHelper extends BasicRepoCommonHelper {

    public static Optional<Customer> fetchById(Connection conn, UUID customerId) throws SQLException {
        String sql = "SELECT * FROM customers WHERE NOT blocked AND id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, customerId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }

                UUID tenantId = UUID.fromString(rs.getString("tenant_id"));
                String name = rs.getString("name");

                Customer customer = new Customer(customerId, tenantId, name);

                return Optional.of(customer);
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
                stmt.setNull(1, Types.OTHER); // _driver_id
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

    public static void block(Connection conn, UUID customerId) throws SQLException {
        String sql = "UPDATE customers SET blocked = true WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, customerId);
            stmt.executeUpdate();
        }
    }
}
