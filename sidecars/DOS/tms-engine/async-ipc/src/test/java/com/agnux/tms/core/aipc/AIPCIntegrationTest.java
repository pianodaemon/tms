package com.agnux.tms.core.aipc;

import com.agnux.tms.api.config.TestSecurityConfig;
import com.agnux.tms.api.dto.AgreementDto;
import com.agnux.tms.api.dto.BoxDto;
import com.agnux.tms.api.dto.CustomerDto;
import com.agnux.tms.api.dto.DriverDto;
import com.agnux.tms.api.dto.PatioDto;
import com.agnux.tms.api.dto.VehicleDto;
import com.agnux.tms.reference.qualitative.BoxBrand;
import com.agnux.tms.reference.qualitative.BoxType;
import com.agnux.tms.reference.quantitative.DistUnit;
import com.agnux.tms.reference.qualitative.VehicleColor;
import com.agnux.tms.reference.qualitative.VehicleType;
import com.agnux.tms.reference.quantitative.VolUnit;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils;

@ActiveProfiles("test")
@SpringBootTest(classes = {AIPCApplication.class}, properties = {
    "debug=true",
    "logging.level.org.springframework.web=DEBUG",
    "logging.level.org.springframework.web.reactive=DEBUG",
    "logging.level.org.springframework.boot.autoconfigure=DEBUG",
    "logging.level.com.agnux.tms=DEBUG"
}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Testcontainers
class AIPCRouterIntegrationTest {

    private static final String AUTH_HEADER_NAME = "Authorization";

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private TestSecurityConfig tsConfig;

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

        UUID tenantId = tsConfig.getFakeTenantId();
        String prefixPathWithTenant = String.format("/adm/%s/drivers", tenantId);

        DriverDto newDriver = new DriverDto(null, "Integration Test Driver", "Tyson", "Wallas", "D123456789");

        var response = webTestClient.post()
                .uri(prefixPathWithTenant)
                .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(newDriver)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(DriverDto.class)
                .returnResult();

        DriverDto createdDriver = response.getResponseBody();
        assert createdDriver != null : "Created driver should not be null";
        assert "Integration Test Driver".equals(createdDriver.getName());
        assert "Tyson".equals(createdDriver.getFirstSurname());
        assert "Wallas".equals(createdDriver.getSecondSurname());
        assert "D123456789".equals(createdDriver.getLicenseNumber());

        final UUID newID = createdDriver.getId();

        webTestClient.get()
                .uri(prefixPathWithTenant + "/" + newID)
                .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.name").isEqualTo("Integration Test Driver");

        webTestClient.delete()
                .uri(prefixPathWithTenant + "/" + newID)
                .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.get()
                .uri(prefixPathWithTenant + "/" + newID)
                .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
                .exchange()
                .expectStatus().isNotFound();

        // --- Pagination assertions ---
        List<UUID> createdDriverIds = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            char letter = (char) (96 + i);
            String pepper = String.valueOf(letter);
            DriverDto d = new DriverDto(null, "Paginated Driver Tuk" + pepper, "RFC" + pepper, "LIC" + pepper, "REG" + pepper);
            var res = webTestClient.post()
                    .uri(prefixPathWithTenant)
                    .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(d)
                    .exchange()
                    .expectStatus()
                    .isOk()
                    .expectBody(DriverDto.class)
                    .returnResult();

            DriverDto created = res.getResponseBody();
            assert created != null;
            createdDriverIds.add(created.getId());
        }

        // Page 1, size 4
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                .path(prefixPathWithTenant)
                .queryParam("page_size", "4")
                .queryParam("page_number", "1")
                .build())
                .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.data.length()").isEqualTo(4)
                .jsonPath("$.totalElements").isEqualTo(6)
                .jsonPath("$.totalPages").isEqualTo(2);

        // Page 2, size 4
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                .path(prefixPathWithTenant)
                .queryParam("page_size", "4")
                .queryParam("page_number", "2")
                .build())
                .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.data.length()").isEqualTo(2)
                .jsonPath("$.totalElements").isEqualTo(6)
                .jsonPath("$.totalPages").isEqualTo(2);

        // Cleanup
        for (UUID id : createdDriverIds) {
            webTestClient.delete()
                    .uri(prefixPathWithTenant + "/" + id)
                    .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
                    .exchange()
                    .expectStatus().isNoContent();

            webTestClient.get()
                    .uri(prefixPathWithTenant + "/" + id)
                    .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
                    .exchange()
                    .expectStatus().isNotFound();
        }
    }

    @Test
    void testCreateAndGetPatio() {
        UUID tenantId = tsConfig.getFakeTenantId();
        String prefixPathWithTenant = String.format("/adm/%s/patios", tenantId);

        var newPatio = new PatioDto(null, "Integration Test Patio", 19.4326, -99.1332);

        var response = webTestClient.post()
                .uri(prefixPathWithTenant)
                .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(newPatio)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(PatioDto.class)
                .returnResult();

        PatioDto createdPatio = response.getResponseBody();
        assert createdPatio != null : "Created patio should not be null";
        assert "Integration Test Patio".equals(createdPatio.getName());
        assert createdPatio.getLatitudeLocation() == 19.4326;
        assert createdPatio.getLongitudeLocation() == -99.1332;

        final UUID newID = createdPatio.getId();

        webTestClient.get()
                .uri(prefixPathWithTenant + "/" + newID)
                .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.name").isEqualTo("Integration Test Patio")
                .jsonPath("$.latitudeLocation").isEqualTo(19.4326)
                .jsonPath("$.longitudeLocation").isEqualTo(-99.1332);

        webTestClient.delete()
                .uri(prefixPathWithTenant + "/" + newID)
                .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.get()
                .uri(prefixPathWithTenant + "/" + newID)
                .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
                .exchange()
                .expectStatus().isNotFound();

        // --- Pagination assertions ---
        List<UUID> createdPatioIds = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            PatioDto patio = new PatioDto(null, "Paginated Patio " + i, 20.0 + i, -100.0 - i);
            var res = webTestClient.post()
                    .uri(prefixPathWithTenant)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(patio)
                    .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(PatioDto.class)
                    .returnResult();

            PatioDto created = res.getResponseBody();
            assert created != null;
            createdPatioIds.add(created.getId());
        }

        // Page 1, size 3
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                .path(prefixPathWithTenant)
                .queryParam("page_size", "3")
                .queryParam("page_number", "1")
                .build())
                .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.data.length()").isEqualTo(3)
                .jsonPath("$.totalElements").isEqualTo(5)
                .jsonPath("$.totalPages").isEqualTo(2);

        // Page 2, size 3
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                .path(prefixPathWithTenant)
                .queryParam("page_size", "3")
                .queryParam("page_number", "2")
                .build())
                .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.data.length()").isEqualTo(2)
                .jsonPath("$.totalElements").isEqualTo(5)
                .jsonPath("$.totalPages").isEqualTo(2);

        // Cleanup
        for (UUID id : createdPatioIds) {
            webTestClient.delete()
                    .uri(prefixPathWithTenant + "/" + id)
                    .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
                    .exchange()
                    .expectStatus().isNoContent();

            webTestClient.get()
                    .uri(prefixPathWithTenant + "/" + id)
                    .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
                    .exchange()
                    .expectStatus().isNotFound();
        }
    }

    @Test
    void testCreateAndGetCustomer() {

        UUID tenantId = tsConfig.getFakeTenantId();
        String prefixPathWithTenant = String.format("/adm/%s/customers", tenantId);

        var newCustomer = new CustomerDto(null, "Integration Test Customer");

        var response = webTestClient.post()
                .uri(prefixPathWithTenant)
                .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(newCustomer)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(CustomerDto.class)
                .returnResult();

        CustomerDto createdCustomer = response.getResponseBody();
        assert createdCustomer != null : "Created dto customer should not be null";
        assert "Integration Test Customer".equals(createdCustomer.getName());

        webTestClient.get()
                .uri(prefixPathWithTenant + "/" + createdCustomer.getId())
                .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.name").isEqualTo("Integration Test Customer");

        webTestClient.delete()
                .uri(prefixPathWithTenant + "/" + createdCustomer.getId())
                .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.get()
                .uri(prefixPathWithTenant + "/" + createdCustomer.getId())
                .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
                .exchange()
                .expectStatus().isNotFound();

        {
            List<UUID> createdCustomerIds = new ArrayList<>();

            // Create multiple customers and collect their IDs
            for (int i = 1; i <= 5; i++) {
                CustomerDto customer = new CustomerDto(null, "Paginated Customer " + i);
                var res = webTestClient.post()
                        .uri(prefixPathWithTenant)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(customer)
                        .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
                        .exchange()
                        .expectStatus().isOk()
                        .expectBody(CustomerDto.class)
                        .returnResult();

                CustomerDto created = res.getResponseBody();
                assert created != null;
                createdCustomerIds.add(created.getId());
            }

            // Request page 1 with size 3
            webTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                    .path(prefixPathWithTenant)
                    .queryParam("page_size", "3")
                    .queryParam("page_number", "1")
                    .build())
                    .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
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
                    .path(prefixPathWithTenant)
                    .queryParam("page_size", "3")
                    .queryParam("page_number", "2")
                    .build())
                    .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
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
                        .uri(prefixPathWithTenant + "/" + id)
                        .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
                        .exchange()
                        .expectStatus().isNoContent();

                webTestClient.get()
                        .uri(prefixPathWithTenant + "/" + id)
                        .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
                        .exchange()
                        .expectStatus().isNotFound();
            }
        }

        {
            List<String> names = List.of("Anna", "Brian", "Charlie", "Diana", "Edward");
            List<UUID> createdCustomerIds = new ArrayList<>();

            // Create customers and collect their IDs
            for (String name : names) {
                CustomerDto customer = new CustomerDto(null, name);
                var res = webTestClient.post()
                        .uri(prefixPathWithTenant)
                        .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(customer)
                        .exchange()
                        .expectStatus().isOk()
                        .expectBody(CustomerDto.class)
                        .returnResult();

                CustomerDto created = res.getResponseBody();
                assert created != null;
                createdCustomerIds.add(created.getId());
            }

            // === ASCENDING ORDER PAGE 1 ===
            webTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                    .path(prefixPathWithTenant)
                    .queryParam("page_size", "3")
                    .queryParam("page_number", "1")
                    .queryParam("page_order_by", "name")
                    .build())
                    .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
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
                    .path(prefixPathWithTenant)
                    .queryParam("page_size", "3")
                    .queryParam("page_number", "1")
                    .queryParam("page_order_by", "name")
                    .queryParam("page_order", "DESC")
                    .build())
                    .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
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
                    .path(prefixPathWithTenant)
                    .queryParam("page_number", "1")
                    .queryParam("page_order_by", "name")
                    .queryParam("page_order", "DESC")
                    .queryParam("filter_qu_name", "%arl%")
                    .build())
                    .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.data.length()").isEqualTo(1)
                    .jsonPath("$.data[0].name").isEqualTo("Charlie");

            // === FILTER: Non-matching query ===
            webTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                    .path(prefixPathWithTenant)
                    .queryParam("filter_qu_name", "zzzzzz")
                    .build())
                    .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
                    .exchange()
                    .expectStatus().isNotFound();

            // === Cleanup: Delete all created customers ===
            for (UUID id : createdCustomerIds) {
                webTestClient.delete()
                        .uri(prefixPathWithTenant + "/" + id)
                        .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
                        .exchange()
                        .expectStatus().isNoContent();

                webTestClient.get()
                        .uri(prefixPathWithTenant + "/" + id)
                        .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
                        .exchange()
                        .expectStatus().isNotFound();
            }
        }
        {
            // ===  Negative Assertion (Invalid names)
            List<String> invalidNames = List.of(
                    "",
                    "   ",
                    "Middle..Dot",
                    ".StartsWithDot",
                    "Miguel && Miguel",
                    "inV.a.l.i.d.&C.h.a.r.s.",
                    "invV.a.l.i.d&.C.h.a.r.s.",
                    "!InvalidChars",
                    "&InvalidChars",
                    "|InvalidChars",
                    "Invalid!Chars",
                    "Invalid&&Chars",
                    "Invalid|Chars"
            );

            for (String invalidName : invalidNames) {
                var invalidCustomer = new CustomerDto(null, invalidName);
                webTestClient.post()
                        .uri(prefixPathWithTenant)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(invalidCustomer)
                        .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
                        .exchange()
                        .expectStatus().isBadRequest(); // assuming INVALID_DATA maps to 400
            }

            // === Positive Assertion (Valid names)
            List<String> validNames = List.of(
                    "Alpha",
                    "Beta One",
                    "Gamma.Inc",
                    "Dr. Who",
                    "John A. Smith",
                    "Acme Corp.",
                    "Z",
                    "A.B",
                    "Jane Doe",
                    "Procter & Gamble",
                    "J&P Inc. cars",
                    " 33 Piano bar ", // It will be normalized
                    "Invalid  Name", // double spaces, It will be normalized
                    "EndsWithSpace " // It will be normalized
            );

            for (String validName : validNames) {
                var validCustomer = new CustomerDto(null, validName);
                var validRes = webTestClient.post()
                        .uri(prefixPathWithTenant)
                        .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(validCustomer)
                        .exchange()
                        .expectStatus().isOk()
                        .expectBody(CustomerDto.class)
                        .returnResult();

                CustomerDto createdValid = validRes.getResponseBody();
                assert createdValid != null : "Customer creation failed for valid name: " + validName;

                // cleanup
                webTestClient.delete()
                        .uri(prefixPathWithTenant + "/" + createdValid.getId())
                        .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
                        .exchange()
                        .expectStatus().isNoContent();
            }
        }
    }

    @Test
    void testCreateAndGetVehicle() {

        UUID tenantId = tsConfig.getFakeTenantId();
        String prefixPathWithTenant = String.format("/adm/%s/vehicles", tenantId);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, 24);
        Date expirationDate = calendar.getTime();

        VehicleDto newVehicle = new VehicleDto(
                null,
                "ABC1234",
                expirationDate,
                "ASDXXXX001",
                1,
                VehicleType.DRY_VAN,
                VehicleColor.GRAY,
                2025,
                expirationDate,
                "XA",
                DistUnit.KM,
                VolUnit.LT,
                new BigDecimal("100")
        );

        var response = webTestClient.post()
                .uri(prefixPathWithTenant)
                .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(newVehicle)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(VehicleDto.class)
                .returnResult();

        VehicleDto createdVehicule = response.getResponseBody();
        assert createdVehicule != null : "Created vehicle should not be null";
        assert "ABC1234".equals(createdVehicule.getNumberPlate());
        assert "ASDXXXX001".equals(createdVehicule.getNumberSerial());
        assert 2025 == createdVehicule.getVehicleYear();
        assert createdVehicule.getVehicleColor() == VehicleColor.GRAY;
        assert createdVehicule.getPerfVolUnit() == VolUnit.LT;
        assert createdVehicule.getPerfDistUnit() == DistUnit.KM;

        final UUID newID = createdVehicule.getId();

        webTestClient.get()
                .uri(prefixPathWithTenant + "/" + newID)
                .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.numberPlate").isEqualTo("ABC1234")
                .jsonPath("$.numberSerial").isEqualTo("ASDXXXX001")
                .jsonPath("$.vehicleType").isEqualTo("DRY_VAN")
                .jsonPath("$.vehicleColor").isEqualTo("GRAY")
                .jsonPath("$.vehicleYear").isEqualTo(2025)
                .jsonPath("$.perfVolUnit").isEqualTo("LT")
                .jsonPath("$.perfDistUnit").isEqualTo("KM")
                .jsonPath("$.perfScalar").isEqualTo(100);

        webTestClient.delete()
                .uri(prefixPathWithTenant + "/" + newID)
                .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.get()
                .uri(prefixPathWithTenant + "/" + newID)
                .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
                .exchange()
                .expectStatus().isNotFound();

        // --- Pagination test ---
        {
            List<UUID> createdVehicleIds = new ArrayList<>();
            for (int i = 1; i <= 7; i++) {
                VehicleDto v = new VehicleDto(
                        null,
                        "PLATE" + i,
                        expirationDate,
                        "SERIAL-" + i,
                        1,
                        VehicleType.DRY_VAN,
                        VehicleColor.GRAY,
                        2023,
                        expirationDate,
                        "XB",
                        DistUnit.MI,
                        VolUnit.GAL,
                        new BigDecimal("100")
                );

                var res = webTestClient.post()
                        .uri(prefixPathWithTenant)
                        .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(v)
                        .exchange()
                        .expectStatus().isOk()
                        .expectBody(VehicleDto.class)
                        .returnResult();

                VehicleDto created = res.getResponseBody();
                assert created != null : "Created vehicle should not be null";
                createdVehicleIds.add(created.getId());
            }

            // PAGE 1, size 5
            webTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                    .path(prefixPathWithTenant)
                    .queryParam("page_size", "5")
                    .queryParam("page_number", "1")
                    .queryParam("filter_qu_vehicle_type", "DRY_VAN")
                    .queryParam("filter_qu_vehicle_color", "%RAY")
                    .queryParam("filter_ge_vehicle_year", 2023)
                    .build())
                    .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody()
                    .jsonPath("$.data.length()").isEqualTo(5)
                    .jsonPath("$.totalElements").isEqualTo(7)
                    .jsonPath("$.totalPages").isEqualTo(2);

            // PAGE 2, size 5
            webTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                    .path(prefixPathWithTenant)
                    .queryParam("tenant_id", tenantId.toString())
                    .queryParam("page_size", "5")
                    .queryParam("page_number", "2")
                    .build())
                    .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody()
                    .jsonPath("$.data.length()").isEqualTo(2)
                    .jsonPath("$.totalElements").isEqualTo(7)
                    .jsonPath("$.totalPages").isEqualTo(2);

            // Cleanup
            for (UUID id : createdVehicleIds) {
                webTestClient.delete()
                        .uri(prefixPathWithTenant + "/" + id)
                        .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
                        .exchange()
                        .expectStatus().isNoContent();

                webTestClient.get()
                        .uri(prefixPathWithTenant + "/" + id)
                        .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
                        .exchange()
                        .expectStatus().isNotFound();
            }
        }
    }

    @Test
    void testCreateAndGetAgreement() {
        UUID tenantId = tsConfig.getFakeTenantId();

        // --- Create a customer first ---
        CustomerDto newCustomer = new CustomerDto(null, "Integration Test Customer");

        var response = webTestClient.post()
                .uri(String.format("/adm/%s/customers", tenantId))
                .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(newCustomer)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(CustomerDto.class)
                .returnResult();

        CustomerDto createdCustomer = response.getResponseBody();
        assert createdCustomer != null : "Created customer should not be null";
        UUID customerId = createdCustomer.getId();

        String prefixPathWithTenant = String.format("/adm/%s/agreements", tenantId);

        // --- Create an agreement ---
        AgreementDto agreement = new AgreementDto(null, customerId, "Receiver X",
                19.4326, -99.1332, 20.6597, -103.3496, DistUnit.KM, new BigDecimal("533.2"));

        var agreementResponse = webTestClient.post()
                .uri(prefixPathWithTenant)
                .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(agreement)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AgreementDto.class)
                .returnResult();

        AgreementDto createdAgreement = agreementResponse.getResponseBody();
        assert createdAgreement != null;
        UUID agreementId = createdAgreement.getId();

        // --- Read (GET) the agreement ---
        webTestClient.get()
                .uri(prefixPathWithTenant + "/" + agreementId)
                .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.receiver").isEqualTo("Receiver X");

        // --- Update the agreement ---
        createdAgreement.setReceiver("Updated Receiver");
        createdAgreement.setDistScalar(new BigDecimal("999.99"));

        webTestClient.put()
                .uri(prefixPathWithTenant)
                .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createdAgreement)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.receiver").isEqualTo("Updated Receiver")
                .jsonPath("$.distScalar").isEqualTo(999.99);

        // --- Pagination Test ---
        List<UUID> createdAgreementIds = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            AgreementDto ag = new AgreementDto(null, customerId, "Receiver " + i,
                    10 + i, -10 - i, 20 + i, -20 - i, DistUnit.KM, new BigDecimal("100." + i));
            var result = webTestClient.post()
                    .uri(prefixPathWithTenant)
                    .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(ag)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(AgreementDto.class)
                    .returnResult();

            AgreementDto created = result.getResponseBody();
            assert created != null;
            createdAgreementIds.add(created.getId());
        }

        // Page 1, size 4
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                .path(prefixPathWithTenant)
                .queryParam("page_size", "4")
                .queryParam("page_number", "1")
                .build())
                .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.length()").isEqualTo(4)
                .jsonPath("$.totalElements").isEqualTo(7)
                .jsonPath("$.totalPages").isEqualTo(2);

        // Page 2, size 4
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                .path(prefixPathWithTenant)
                .queryParam("page_size", "4")
                .queryParam("page_number", "2")
                .build())
                .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.length()").isEqualTo(3)
                .jsonPath("$.totalElements").isEqualTo(7)
                .jsonPath("$.totalPages").isEqualTo(2);

        // --- Delete all agreements ---
        for (UUID id : createdAgreementIds) {
            webTestClient.delete()
                    .uri(prefixPathWithTenant + "/" + id)
                    .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
                    .exchange()
                    .expectStatus().isNoContent();
        }

        webTestClient.delete()
                .uri(prefixPathWithTenant + "/" + agreementId)
                .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
                .exchange()
                .expectStatus().isNoContent();

        // --- Delete the customer ---
        webTestClient.delete()
                .uri(String.format("/adm/%s/customers", tenantId) + "/" + customerId)
                .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void testCreateAndGetBox() {

        UUID tenantId = tsConfig.getFakeTenantId();
        String prefixPathWithTenant = String.format("/adm/%s/boxes", tenantId);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, 24);
        Date expirationDate = calendar.getTime();
        var newBox = new BoxDto(null, "Integration Test Box", BoxType.UNKNOWN_A, BoxBrand.CIMC , 1,RandomStringUtils.randomAlphanumeric(20), "LFU000001", expirationDate, 2025, false);

        var response = webTestClient.post()
                .uri(prefixPathWithTenant)
                .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(newBox)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(BoxDto.class)
                .returnResult();

        BoxDto createdBox = response.getResponseBody();
        assert createdBox != null : "Created dto box should not be null";
        assert "Integration Test Box".equals(createdBox.getName());

        webTestClient.get()
                .uri(prefixPathWithTenant + "/" + createdBox.getId())
                .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.name").isEqualTo("Integration Test Box");

        webTestClient.delete()
                .uri(prefixPathWithTenant + "/" + createdBox.getId())
                .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.get()
                .uri(prefixPathWithTenant + "/" + createdBox.getId())
                .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
                .exchange()
                .expectStatus().isNotFound();

        {
            List<UUID> createdBoxIds = new ArrayList<>();

            // Create multiple boxs and collect their IDs
            for (int i = 1; i <= 5; i++) {
                BoxDto box = new BoxDto(null, "Paginated Box " + i, BoxType.UNKNOWN_A, BoxBrand.FRUEHAUF, 1, RandomStringUtils.randomAlphanumeric(20), String.format("LFL00000%s", i), expirationDate, 2025, false);
                var res = webTestClient.post()
                        .uri(prefixPathWithTenant)
                        .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(box)
                        .exchange()
                        .expectStatus().isOk()
                        .expectBody(BoxDto.class)
                        .returnResult();

                BoxDto created = res.getResponseBody();
                assert created != null;
                createdBoxIds.add(created.getId());
            }

            // Request page 1 with size 3
            webTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                    .path(prefixPathWithTenant)
                    .queryParam("page_size", "3")
                    .queryParam("page_number", "1")
                    .build())
                    .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
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
                    .path(prefixPathWithTenant)
                    .queryParam("page_size", "3")
                    .queryParam("page_number", "2")
                    .build())
                    .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody()
                    .jsonPath("$.data.length()").isEqualTo(2)
                    .jsonPath("$.totalElements").isEqualTo(5)
                    .jsonPath("$.totalPages").isEqualTo(2);

            // Cleanup: delete all created boxs
            for (UUID id : createdBoxIds) {
                webTestClient.delete()
                        .uri(prefixPathWithTenant + "/" + id)
                        .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
                        .exchange()
                        .expectStatus().isNoContent();

                webTestClient.get()
                        .uri(prefixPathWithTenant + "/" + id)
                        .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
                        .exchange()
                        .expectStatus().isNotFound();
            }
        }

        {
            List<String> names = List.of("FalconA", "FalconB", "FalconC", "FalconD", "FalconE");
            List<UUID> createdBoxIds = new ArrayList<>();

            // Create boxs and collect their IDs
            for (String name : names) {
                BoxDto box = new BoxDto(null, name, BoxType.UNKNOWN_A, BoxBrand.GREAT_DANE, 1, RandomStringUtils.randomAlphanumeric(20), String.format("LFL40000%c", name.charAt(name.length() - 1)), expirationDate, 2025, false);
                var res = webTestClient.post()
                        .uri(prefixPathWithTenant)
                        .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(box)
                        .exchange()
                        .expectStatus().isOk()
                        .expectBody(BoxDto.class)
                        .returnResult();

                BoxDto created = res.getResponseBody();
                assert created != null;
                createdBoxIds.add(created.getId());
            }

            // === ASCENDING ORDER PAGE 1 ===
            webTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                    .path(prefixPathWithTenant)
                    .queryParam("page_size", "3")
                    .queryParam("page_number", "1")
                    .queryParam("page_order_by", "name")
                    .build())
                    .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.data.length()").isEqualTo(3)
                    .jsonPath("$.data[0].name").isEqualTo("FalconA")
                    .jsonPath("$.data[1].name").isEqualTo("FalconB")
                    .jsonPath("$.data[2].name").isEqualTo("FalconC")
                    .jsonPath("$.totalElements").isEqualTo(5)
                    .jsonPath("$.totalPages").isEqualTo(2);

            // === DESCENDING ORDER PAGE 1 ===
            webTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                    .path(prefixPathWithTenant)
                    .queryParam("page_size", "3")
                    .queryParam("page_number", "1")
                    .queryParam("page_order_by", "name")
                    .queryParam("page_order", "DESC")
                    .build())
                    .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.data.length()").isEqualTo(3)
                    .jsonPath("$.data[0].name").isEqualTo("FalconE")
                    .jsonPath("$.data[1].name").isEqualTo("FalconD")
                    .jsonPath("$.data[2].name").isEqualTo("FalconC");

            // === FILTERS ===
            webTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                    .path(prefixPathWithTenant)
                    .queryParam("page_number", "1")
                    .queryParam("page_order_by", "name")
                    .queryParam("page_order", "DESC")
                    .queryParam("filter_qu_name", "%conC%")
                    .build())
                    .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.data.length()").isEqualTo(1)
                    .jsonPath("$.data[0].name").isEqualTo("FalconC");

            // === FILTER: Non-matching query ===
            webTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                    .path(prefixPathWithTenant)
                    .queryParam("filter_qu_name", "zzzzzz")
                    .build())
                    .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
                    .exchange()
                    .expectStatus().isNotFound();

            // === Cleanup: Delete all created boxes ===
            for (UUID id : createdBoxIds) {
                webTestClient.delete()
                        .uri(prefixPathWithTenant + "/" + id)
                        .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
                        .exchange()
                        .expectStatus().isNoContent();

                webTestClient.get()
                        .uri(prefixPathWithTenant + "/" + id)
                        .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
                        .exchange()
                        .expectStatus().isNotFound();
            }
        }
        {
            // ===  Negative Assertion (Invalid names)
            List<String> invalidNames = List.of(
                    "",
                    "   ",
                    "Middle..Dot",
                    ".StartsWithDot",
                    "White -- Eagle",
                    "inV.a.l.i.d.-C.h.a.r.s.",
                    "invV.a.l.i.d-.C.h.a.r.s.",
                    "!InvalidChars",
                    "-InvalidChars",
                    "|InvalidChars",
                    "Invalid!Chars",
                    "Invalid--Chars",
                    "Invalid|Chars"
            );

            int counteryy = 0;
            for (String invalidName : invalidNames) {
                var invalidBox = new BoxDto(null, invalidName, BoxType.UNKNOWN_A, BoxBrand.SCHMITZ_CARGOBULL, 1, RandomStringUtils.randomAlphanumeric(20), String.format("LFL50000%d", counteryy), expirationDate, 2025, false);
                webTestClient.post()
                        .uri(prefixPathWithTenant)
                        .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(invalidBox)
                        .exchange()
                        .expectStatus().isBadRequest(); // assuming INVALID_DATA maps to 400
            }

            // === Positive Assertion (Valid names)
            List<String> validNames = List.of(
                    "Alpha",
                    "Beta One",
                    "Gamma.Inc",
                    "Double Hooker",
                    "Long A. 100-P",
                    "Acme Corp.",
                    "Z",
                    "A.B",
                    "Jane Doe",
                    "Procter - Gamble",
                    "J-P Inc.300",
                    " 69 Stallone ", // It will be normalized
                    "Invalid  Name", // double spaces, It will be normalized
                    "EndsWithSpace " // It will be normalized
            );

            int counterxx = 0;
            for (String validName : validNames) {
                var validBox = new BoxDto(null, validName, BoxType.UNKNOWN_A, BoxBrand.UTILITY_TRAILER, 1, RandomStringUtils.randomAlphanumeric(20) ,String.format("LFL70000%d", counterxx), expirationDate, 2025, false);
                var validRes = webTestClient.post()
                        .uri(prefixPathWithTenant)
                        .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(validBox)
                        .exchange()
                        .expectStatus().isOk()
                        .expectBody(BoxDto.class)
                        .returnResult();

                BoxDto createdValid = validRes.getResponseBody();
                assert createdValid != null : "Box creation failed for valid name: " + validName;

                // cleanup
                webTestClient.delete()
                        .uri(prefixPathWithTenant + "/" + createdValid.getId())
                        .header(AUTH_HEADER_NAME, tsConfig.getFakeAuthHeaderVal())
                        .exchange()
                        .expectStatus().isNoContent();
            }
        }
    }

    @AfterAll
    static void tearDown() {
        postgresContainer.stop();
    }
}
