package com.agnux.haul.repository;

import com.agnux.haul.repository.model.Vehicle;
import com.agnux.haul.repository.model.VehicleType;
import com.agnux.haul.repository.model.DistUnit;
import com.agnux.haul.repository.model.VolUnit;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.sql.DataSource;
import org.postgresql.ds.PGSimpleDataSource;
import org.flywaydb.core.Flyway;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class IHaulRepoImplTest {

    static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    static DataSource dataSource;

    @BeforeAll
    static void setUp() throws Exception {
        postgresContainer.start();

        Flyway flyway = Flyway.configure()
                .dataSource(postgresContainer.getJdbcUrl(), postgresContainer.getUsername(), postgresContainer.getPassword())
                .locations("classpath:db/migration") // Assuming your migration files are in src/main/resources/db/migration
                .load();
        flyway.migrate();

        PGSimpleDataSource ds = new PGSimpleDataSource();
        ds.setUrl(postgresContainer.getJdbcUrl());
        ds.setUser(postgresContainer.getUsername());
        ds.setPassword(postgresContainer.getPassword());

        dataSource = ds;
    }

    @Test
    void testUpdateVehicle_insert_success() throws SQLException {
        UUID tenantId = UUID.randomUUID(); // Generate a valid tenant_id

        Vehicle vehicle = new Vehicle(
                tenantId,
                "ABC-123",
                VehicleType.REFRIGERATED_VAN
        );

        vehicle.setPerfDistUnit(DistUnit.KM);
        vehicle.setPerfVolUnit(VolUnit.LT);
        vehicle.setPerfScalar(new BigDecimal("7.50"));

        try (Connection conn = dataSource.getConnection()) {

            // Call the function
            UUID id = IHaulRepoImpl.updateVehicle(conn, true, vehicle);
            assertNotNull(id, "Returned vehicle ID should not be null");

            // Query and validate all fields
            try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM vehicles WHERE id = ?")) {
                ps.setObject(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    assertTrue(rs.next(), "Vehicle record should exist");

                    assertEquals(id, UUID.fromString(rs.getString("id")));
                    assertEquals(tenantId, UUID.fromString(rs.getString("tenant_id")));
                    assertEquals("ABC-123", rs.getString("number_plate"));
                    assertEquals("REFRIGERATED_VAN", rs.getString("vehicle_type"));
                    assertEquals("KM", rs.getString("perf_dist_unit"));
                    assertEquals("LT", rs.getString("perf_vol_unit"));
                    assertEquals(new BigDecimal("7.50"), rs.getBigDecimal("perf_scalar"));
                }
            }
        }
    }

    @AfterAll
    static void tearDown() {
        postgresContainer.stop();
    }
}
