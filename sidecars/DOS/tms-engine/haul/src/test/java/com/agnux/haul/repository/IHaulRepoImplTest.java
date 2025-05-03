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
        try (Connection connection = dataSource.getConnection(); Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery("SELECT 1 FROM pg_proc WHERE proname = 'alter_vehicle'")) {
            boolean functionExists = rs.next();
            assertTrue(functionExists, "Alter vehicle function should exist before calling updateVehicle");
        }

        Vehicle vehicle = new Vehicle(
                UUID.fromString("4a232802-d6e8-458f-9eca-6a8c2b980982"),
                "ABC-123",
                VehicleType.REFRIGERATED_VAN
        );
        vehicle.setPerfDistUnit(DistUnit.KM);
        vehicle.setPerfVolUnit(VolUnit.LT);
        vehicle.setPerfScalar(new BigDecimal("7.50"));

        UUID id = IHaulRepoImpl.updateVehicle(dataSource.getConnection(), vehicle);
        assertNotNull(id);
    }

    @AfterAll
    static void tearDown() {
        postgresContainer.stop();
    }
}
