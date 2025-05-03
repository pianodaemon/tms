package com.agnux.haul.repository;

import com.agnux.haul.errors.ErrorCodes;
import com.agnux.haul.errors.TmsException;
import com.agnux.haul.repository.model.Customer;
import com.agnux.haul.repository.model.Driver;
import com.agnux.haul.repository.model.Vehicle;
import com.agnux.haul.repository.model.VehicleType;
import com.agnux.haul.repository.model.DistUnit;
import com.agnux.haul.repository.model.Patio;
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
    void testDriver_crud_success() throws SQLException, TmsException {
        UUID tenantId = UUID.randomUUID();

        Driver driver = new Driver(
                null,
                tenantId,
                "Juan Pérez",
                "D123456789"
        );

        UUID driverId = repo.createDriver(driver);
        Driver retrieved = repo.getAvailableDriver(driverId);

        assertEquals(driverId, retrieved.getId().get(), "Driver ID should match");
        assertNotNull(retrieved, "Retrieved driver should not be null");
        assertEquals(driver.getTenantId(), retrieved.getTenantId(), "Tenant ID should match");
        assertEquals(driver.getName(), retrieved.getName(), "Driver name should match");
        assertEquals(driver.getLicenseNumber(), retrieved.getLicenseNumber(), "License number should match");

        // Modify and update
        retrieved.setName("Carlos García");
        retrieved.setLicenseNumber("D987654321");
        repo.editDriver(retrieved);

        Driver updated = repo.getAvailableDriver(driverId);
        assertEquals("Carlos García", updated.getName(), "Updated name should match");
        assertEquals("D987654321", updated.getLicenseNumber(), "Updated license number should match");

        // Delete (block) the driver
        repo.deleteDriver(driverId);

        TmsException ex = assertThrows(TmsException.class, () -> repo.getAvailableDriver(driverId));
        assertEquals(ErrorCodes.REPO_PROVIDEER_ISSUES.getCode(), ex.getErrorCode(), "Expected error code on deleted driver");
    }

    @Test
    void testVehicle_crud_success() throws SQLException, TmsException {
        UUID tenantId = UUID.randomUUID(); // Generate a valid tenant_id

        Vehicle vehicle = new Vehicle(
                null,
                tenantId,
                "ABC-123",
                VehicleType.REFRIGERATED_VAN
        );

        vehicle.setPerfDistUnit(DistUnit.KM);
        vehicle.setPerfVolUnit(VolUnit.LT);
        vehicle.setPerfScalar(new BigDecimal("7.50"));

        final UUID id = repo.createVehicle(vehicle);
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

        // Now, delete the vehicle (aka block)
        repo.deleteVehicle(id);

        // It can not retrieve the updated vehicle
        TmsException assertThrows = assertThrows(TmsException.class, () -> repo.getAvailableVehicule(id), "Blocked vehicle should not be retrievable");
        assertTrue(assertThrows.getErrorCode() == ErrorCodes.REPO_PROVIDEER_ISSUES.getCode(), "Error code is not what we expected");
    }

    @Test
    void testPatio_crud_success() throws SQLException, TmsException {
        UUID tenantId = UUID.randomUUID();

        Patio patio = new Patio(
                null,
                tenantId,
                "Main Patio",
                19.4326,
                -99.1332
        );

        UUID patioId = repo.createPatio(patio);
        Patio retrieved = repo.getAvailablePatio(patioId);

        assertNotNull(retrieved, "Retrieved patio should not be null");
        assertEquals(patioId, retrieved.getId().get(), "Patio ID should match");
        assertEquals(patio.getTenantId(), retrieved.getTenantId(), "Tenant ID should match");
        assertEquals(patio.getName(), retrieved.getName(), "Patio name should match");
        assertEquals(patio.getLatitudeLocation(), retrieved.getLatitudeLocation(), 0.0001, "Latitude should match");
        assertEquals(patio.getLongitudeLocation(), retrieved.getLongitudeLocation(), 0.0001, "Longitude should match");

        // Modify and update
        retrieved.setName("Updated Patio");
        retrieved.setLatitudeLocation(40.7128);
        retrieved.setLongitudeLocation(-74.0060);
        repo.editPatio(retrieved);

        Patio updated = repo.getAvailablePatio(patioId);
        assertEquals("Updated Patio", updated.getName(), "Updated name should match");
        assertEquals(40.7128, updated.getLatitudeLocation(), 0.0001, "Updated latitude should match");
        assertEquals(-74.0060, updated.getLongitudeLocation(), 0.0001, "Updated longitude should match");

        // Delete (block) the patio
        repo.deletePatio(patioId);

        TmsException ex = assertThrows(TmsException.class, () -> repo.getAvailablePatio(patioId));
        assertEquals(ErrorCodes.REPO_PROVIDEER_ISSUES.getCode(), ex.getErrorCode(), "Expected error code on blocked patio");
    }

    @Test
    void testCustomer_crud_success() throws SQLException, TmsException {
        UUID tenantId = UUID.randomUUID(); // Generate a valid tenant_id

        // Create a new customer
        Customer customer = new Customer(
                null,
                tenantId,
                "John Doe"
        );

        // Create customer in the database
        UUID customerId = repo.createCustomer(customer);
        Customer retrieved = repo.getAvailableCustomer(customerId);

        // Assertions to check if customer was created correctly
        assertNotNull(retrieved, "Retrieved customer should not be null");
        assertEquals(customerId, retrieved.getId().get(), "Customer ID should match");
        assertEquals(customer.getTenantId(), retrieved.getTenantId(), "Tenant ID should match");
        assertEquals(customer.getName(), retrieved.getName(), "Customer name should match");

        // Modify and update customer information
        retrieved.setName("Jane Smith");
        repo.editCustomer(retrieved);

        // Retrieve the updated customer
        Customer updated = repo.getAvailableCustomer(customerId);

        // Assertions for updated customer
        assertEquals("Jane Smith", updated.getName(), "Updated name should match");

        // Now, delete (block) the customer
        repo.deleteCustomer(customerId);

        // It should not be possible to retrieve the blocked customer
        TmsException assertThrows = assertThrows(TmsException.class, () -> repo.getAvailableCustomer(customerId), "Blocked customer should not be retrievable");
        assertTrue(assertThrows.getErrorCode() == ErrorCodes.REPO_PROVIDEER_ISSUES.getCode(), "Error code is not what we expected");
    }

    @AfterAll
    static void tearDown() {
        postgresContainer.stop();
    }
}
