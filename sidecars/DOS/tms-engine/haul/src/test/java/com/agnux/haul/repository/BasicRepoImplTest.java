package com.agnux.haul.repository;

import com.agnux.haul.errors.TmsException;
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
import java.sql.SQLException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class BasicRepoImplTest {

    static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    static DataSource dataSource;
    static IHaulRepo repo;

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
        repo = new BasicRepoImpl(dataSource, true);
    }

    @Test
    void testUpdateVehicle_create_and_edit_success() throws SQLException, TmsException {
        UUID tenantId = UUID.randomUUID(); // Generate a valid tenant_id

        Vehicle vehicle = new Vehicle(
                tenantId,
                "ABC-123",
                VehicleType.REFRIGERATED_VAN
        );

        vehicle.setPerfDistUnit(DistUnit.KM);
        vehicle.setPerfVolUnit(VolUnit.LT);
        vehicle.setPerfScalar(new BigDecimal("7.50"));

        UUID id = repo.createVehicle(vehicle);
        Vehicle retrieved = repo.getAvailableVehicule(id);

        assertEquals(id, retrieved.getId().get());
        assertNotNull(vehicle, "vehicle created should not be null");
        assertNotNull(retrieved, "vehicle retrieved should not be null");

        // Compare tenantId
        assertEquals(vehicle.getTenantId(), retrieved.getTenantId(), "Tenant ID should match");

        // Compare numberPlate
        assertEquals(vehicle.getNumberPlate(), retrieved.getNumberPlate(), "Number plate should match");

        // Compare vehicleType
        assertEquals(vehicle.getVehicleType(), retrieved.getVehicleType(), "Vehicle type should match");

        // Compare perfDistUnit
        assertEquals(vehicle.getPerfDistUnit(), retrieved.getPerfDistUnit(), "Performance distance unit should match");

        // Compare perfVolUnit
        assertEquals(vehicle.getPerfVolUnit(), retrieved.getPerfVolUnit(), "Performance volume unit should match");

        // Compare perfScalar
        assertEquals(vehicle.getPerfScalar(), retrieved.getPerfScalar(), "Performance scalar should match");

        // Now, edit the vehicle and update it
        retrieved.setNumberPlate("XYZ-999");
        retrieved.setVehicleType(VehicleType.TANDEM_TRUCK);
        retrieved.setPerfScalar(new BigDecimal("8.00"));

        // Update the vehicle in the database
        repo.editVehicle(retrieved);

        // Retrieve the updated vehicle
        Vehicle updated = repo.getAvailableVehicule(id);

        // Assert the updated vehicle matches the modified values
        assertNotNull(updated, "Updated vehicle should not be null");

        // Compare tenantId
        assertEquals(retrieved.getTenantId(), updated.getTenantId(), "Tenant ID should match after update");

        // Compare numberPlate
        assertEquals(retrieved.getNumberPlate(), updated.getNumberPlate(), "Number plate should match after update");

        // Compare vehicleType
        assertEquals(retrieved.getVehicleType(), updated.getVehicleType(), "Vehicle type should match after update");

        // Compare perfDistUnit
        assertEquals(retrieved.getPerfDistUnit(), updated.getPerfDistUnit(), "Performance distance unit should match after update");

        // Compare perfVolUnit
        assertEquals(retrieved.getPerfVolUnit(), updated.getPerfVolUnit(), "Performance volume unit should match after update");

        // Compare perfScalar
        assertEquals(retrieved.getPerfScalar(), updated.getPerfScalar(), "Performance scalar should match after update");
    }

    @AfterAll
    static void tearDown() {
        postgresContainer.stop();
    }
}
