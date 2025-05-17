package com.agnux.tms.api;

import java.util.UUID;

import com.agnux.tms.repository.BasicRepoImpl;
import com.agnux.tms.repository.model.Driver;
import com.agnux.tms.errors.TmsException;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Component
@RequiredArgsConstructor
@Log4j2
public class DriverHandler {

    private final BasicRepoImpl repo;

    public Mono<ServerResponse> createDriver(ServerRequest request) {
        return request.bodyToMono(Driver.class)
                .flatMap(driver -> {
                    try {
                        UUID newId = repo.createDriver(driver);
                        log.info("Created driver feturing UUID: " + newId.toString());
                        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(driver);
                    } catch (TmsException e) {
                        return ServerResponse.status(500).bodyValue("Creation failed: " + e.getMessage());
                    }
                });
    }

    public Mono<ServerResponse> readDriver(ServerRequest request) {
        UUID driverId = UUID.fromString(request.pathVariable("id"));
        try {
            Driver driver = repo.getDriver(driverId);
            return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(driver);
        } catch (TmsException e) {
            return ServerResponse.notFound().build();
        }
    }

    public Mono<ServerResponse> updateDriver(ServerRequest request) {
        return request.bodyToMono(Driver.class)
                .flatMap(driver -> {
                    try {
                        UUID updatedId = repo.editDriver(driver);
                        log.info("Updated driver feturing UUID: " + updatedId.toString());
                        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(driver);
                    } catch (TmsException e) {
                        return ServerResponse.status(500).bodyValue("Update failed: " + e.getMessage());
                    }
                });
    }

    public Mono<ServerResponse> deleteDriver(ServerRequest request) {
        UUID driverId = UUID.fromString(request.pathVariable("id"));
        try {
            repo.deleteDriver(driverId);
            return ServerResponse.noContent().build();
        } catch (TmsException e) {
            return ServerResponse.status(500).bodyValue("Deletion failed: " + e.getMessage());
        }
    }
}
