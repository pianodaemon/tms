package com.agnux.tms.core.aipc;

import com.agnux.tms.repository.model.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.junit.jupiter.Container;

@SpringBootTest(classes = AIPCApplication.class, properties = {
    "debug=true",
    "logging.level.org.springframework.web=DEBUG",
    "logging.level.org.springframework.web.reactive=DEBUG",
    "logging.level.org.springframework.boot.autoconfigure=DEBUG",
    "logging.level.com.agnux=DEBUG"
})
@AutoConfigureWebTestClient
@Testcontainers
class AIPCRouterIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Container
    static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("aipctestdb")
            .withUsername("aipctestdb")
            .withPassword("aipctestdb");

    @BeforeAll
    static void applyFlywayMigrations() {
        // Ensure the container is running before migration
        if (!postgresContainer.isRunning()) {
            postgresContainer.start();  // usually not needed when @Container is used
        }

        Flyway.configure()
                .dataSource(
                        postgresContainer.getJdbcUrl(),
                        postgresContainer.getUsername(),
                        postgresContainer.getPassword()
                )
                .locations("classpath:db/migration")
                .load()
                .migrate();

    }

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("db.url", postgresContainer::getJdbcUrl);
        registry.add("db.username", postgresContainer::getUsername);
        registry.add("db.password", postgresContainer::getPassword);
    }

    @Test
    void testCreateAndGetDriver() {
        Driver newDriver = new Driver(null, UUID.randomUUID(), "Integration Test Driver", "tyson", "wallas", "D123456789");

        var response = webTestClient.post()
                .uri("/adm/drivers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(newDriver)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Driver.class)
                .returnResult();

        Driver createdDriver = response.getResponseBody();
        assert createdDriver != null : "Created driver should not be null";
        assert "Integration Test Driver".equals(createdDriver.getName());
        assert "tyson".equals(createdDriver.getFirstSurname());
        assert "wallas".equals(createdDriver.getSecondSurname());
        assert "D123456789".equals(createdDriver.getLicenseNumber());

        final UUID newID = createdDriver.getId().orElseThrow();

        System.out.println("/adm/drivers/" + newID);

        webTestClient.get()
                .uri("/adm/drivers/" + newID)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.name").isEqualTo("Integration Test Driver");

        webTestClient.delete()
                .uri("/adm/drivers/" + newID)
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.get()
                .uri("/adm/drivers/" + newID)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testCreateAndGetPatio() {
        UUID tenantId = UUID.randomUUID();
        Patio newPatio = new Patio(null, tenantId, "Integration Test Patio", 19.4326, -99.1332);

        var response = webTestClient.post()
                .uri("/adm/patios")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(newPatio)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Patio.class)
                .returnResult();

        Patio createdPatio = response.getResponseBody();
        assert createdPatio != null : "Created patio should not be null";
        assert "Integration Test Patio".equals(createdPatio.getName());
        assert createdPatio.getLatitudeLocation() == 19.4326;
        assert createdPatio.getLongitudeLocation() == -99.1332;

        final UUID newID = createdPatio.getId().orElseThrow();

        System.out.println("/adm/patios/" + newID);

        webTestClient.get()
                .uri("/adm/patios/" + newID)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.name").isEqualTo("Integration Test Patio")
                .jsonPath("$.latitudeLocation").isEqualTo(19.4326)
                .jsonPath("$.longitudeLocation").isEqualTo(-99.1332);

        webTestClient.delete()
                .uri("/adm/patios/" + newID)
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.get()
                .uri("/adm/patios/" + newID)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testCreateAndGetCustomer() {
        Customer newCustomer = new Customer(null, UUID.randomUUID(), "Integration Test Customer");

        var response = webTestClient.post()
                .uri("/adm/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(newCustomer)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Customer.class)
                .returnResult();

        Customer createdCustomer = response.getResponseBody();
        assert createdCustomer != null : "Created customer should not be null";
        assert "Integration Test Customer".equals(createdCustomer.getName());

        final UUID newID = createdCustomer.getId().orElseThrow();

        System.out.println("/adm/customers/" + newID);

        webTestClient.get()
                .uri("/adm/customers/" + newID)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.name").isEqualTo("Integration Test Customer");

        webTestClient.delete()
                .uri("/adm/customers/" + newID)
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.get()
                .uri("/adm/customers/" + newID)
                .exchange()
                .expectStatus().isNotFound();

        {
            UUID tenantId = UUID.randomUUID();
            List<UUID> createdCustomerIds = new ArrayList<>();

            // Create multiple customers and collect their IDs
            for (int i = 1; i <= 5; i++) {
                Customer customer = new Customer(null, tenantId, "Paginated Customer " + i);
                var res = webTestClient.post()
                        .uri("/adm/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(customer)
                        .exchange()
                        .expectStatus().isOk()
                        .expectBody(Customer.class)
                        .returnResult();

                Customer created = res.getResponseBody();
                assert created != null;
                createdCustomerIds.add(created.getId().orElseThrow());
            }

            // Request page 1 with size 3
            webTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                    .path("/adm/customers")
                    .queryParam("tenant_id", tenantId.toString())
                    .queryParam("page_size", "3")
                    .queryParam("page_number", "1")
                    .build())
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody()
                    .jsonPath("$.data.length()").isEqualTo(3)
                    .jsonPath("$.totalElements").isEqualTo(5)
                    .jsonPath("$.totalPages").isEqualTo(2);

            // Request page 2 with size 3
            webTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                    .path("/adm/customers")
                    .queryParam("tenant_id", tenantId.toString())
                    .queryParam("page_size", "3")
                    .queryParam("page_number", "2")
                    .build())
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody()
                    .jsonPath("$.data.length()").isEqualTo(2)
                    .jsonPath("$.totalElements").isEqualTo(5)
                    .jsonPath("$.totalPages").isEqualTo(2);

            // Cleanup: delete all created customers
            for (UUID id : createdCustomerIds) {
                webTestClient.delete()
                        .uri("/adm/customers/" + id)
                        .exchange()
                        .expectStatus().isNoContent();

                webTestClient.get()
                        .uri("/adm/customers/" + id)
                        .exchange()
                        .expectStatus().isNotFound();
            }
        }

        {
            UUID tenantId = UUID.randomUUID();
            List<String> names = List.of("Anna", "Brian", "Charlie", "Diana", "Edward");
            List<UUID> createdCustomerIds = new ArrayList<>();

            // Create customers and collect their IDs
            for (String name : names) {
                Customer customer = new Customer(null, tenantId, name);
                var res = webTestClient.post()
                        .uri("/adm/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(customer)
                        .exchange()
                        .expectStatus().isOk()
                        .expectBody(Customer.class)
                        .returnResult();

                Customer created = res.getResponseBody();
                assert created != null;
                createdCustomerIds.add(created.getId().orElseThrow());
            }

            // === ASCENDING ORDER PAGE 1 ===
            webTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                    .path("/adm/customers")
                    .queryParam("tenant_id", tenantId.toString())
                    .queryParam("page_size", "3")
                    .queryParam("page_number", "1")
                    .queryParam("page_order_by", "name")
                    .build())
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.data.length()").isEqualTo(3)
                    .jsonPath("$.data[0].name").isEqualTo("Anna")
                    .jsonPath("$.data[1].name").isEqualTo("Brian")
                    .jsonPath("$.data[2].name").isEqualTo("Charlie")
                    .jsonPath("$.totalElements").isEqualTo(5)
                    .jsonPath("$.totalPages").isEqualTo(2);

            // === DESCENDING ORDER PAGE 1 ===
            webTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                    .path("/adm/customers")
                    .queryParam("tenant_id", tenantId.toString())
                    .queryParam("page_size", "3")
                    .queryParam("page_number", "1")
                    .queryParam("page_order_by", "name")
                    .queryParam("page_order", "DESC")
                    .build())
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.data.length()").isEqualTo(3)
                    .jsonPath("$.data[0].name").isEqualTo("Edward")
                    .jsonPath("$.data[1].name").isEqualTo("Diana")
                    .jsonPath("$.data[2].name").isEqualTo("Charlie");

            // === FILTERS ===
            webTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                    .path("/adm/customers")
                    .queryParam("tenant_id", tenantId.toString())
                    .queryParam("page_number", "1")
                    .queryParam("page_order_by", "name")
                    .queryParam("page_order", "DESC")
                    .queryParam("filter_name", "%arl%")
                    .build())
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.data.length()").isEqualTo(1)
                    .jsonPath("$.data[0].name").isEqualTo("Charlie");

            // === Cleanup: Delete all created customers ===
            for (UUID id : createdCustomerIds) {
                webTestClient.delete()
                        .uri("/adm/customers/" + id)
                        .exchange()
                        .expectStatus().isNoContent();

                webTestClient.get()
                        .uri("/adm/customers/" + id)
                        .exchange()
                        .expectStatus().isNotFound();
            }
        }
    }

    @Test
    void testCreateAndGetVehicule() {
        UUID tenantId = UUID.randomUUID();
        Vehicle newVehicle = new Vehicle(
                null,
                tenantId,
                "ABC-1234",
                "ASDXXXX001",
                VehicleType.DRY_VAN,
                VehicleColor.GRAY,
                2025,
                "XA",
                DistUnit.KM,
                VolUnit.LT,
                new BigDecimal("100")
        );

        var response = webTestClient.post()
                .uri("/adm/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(newVehicle)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Vehicle.class)
                .returnResult();

        Vehicle createdVehicule = response.getResponseBody();
        assert createdVehicule != null : "Created vehicule should not be null";
        assert "ABC-1234".equals(createdVehicule.getNumberPlate());
        assert "ASDXXXX001".equals(createdVehicule.getNumberSerial());
        assert 2025 == createdVehicule.getVehicleYear();
        assert createdVehicule.getVehicleColor() == VehicleColor.GRAY;
        assert createdVehicule.getPerfVolUnit() == VolUnit.LT;
        assert createdVehicule.getPerfDistUnit() == DistUnit.KM;

        final UUID newID = createdVehicule.getId().orElseThrow();

        webTestClient.get()
                .uri("/adm/vehicles/" + newID)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.numberPlate").isEqualTo("ABC-1234")
                .jsonPath("$.numberSerial").isEqualTo("ASDXXXX001")
                .jsonPath("$.vehicleType").isEqualTo("DRY_VAN")
                .jsonPath("$.vehicleColor").isEqualTo("GRAY")
                .jsonPath("$.vehicleYear").isEqualTo(2025)
                .jsonPath("$.perfVolUnit").isEqualTo("LT")
                .jsonPath("$.perfDistUnit").isEqualTo("KM")
                .jsonPath("$.perfScalar").isEqualTo(100);

        webTestClient.delete()
                .uri("/adm/vehicles/" + newID)
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.get()
                .uri("/adm/vehicles/" + newID)
                .exchange()
                .expectStatus().isNotFound();
    }

    @AfterAll
    static void tearDown() {
        postgresContainer.stop();
    }
}
