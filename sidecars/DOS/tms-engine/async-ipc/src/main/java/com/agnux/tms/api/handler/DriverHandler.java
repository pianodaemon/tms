package com.agnux.tms.api.handler;

import com.agnux.tms.api.handler.ServiceResponseHelper;
import com.agnux.tms.errors.ErrorCodes;
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
                        log.info("created driver featuring UUID: " + newId.toString());
                        driver.setId(newId);
                        return ServiceResponseHelper.successWithBody(driver);
                    } catch (TmsException e) {
                        if (ErrorCodes.REPO_PROVIDER_ISSUES.getCode() == e.getErrorCode()) {
                            return ServiceResponseHelper.badRequest("data supplied face issues", e);
                        }
                        return ServiceResponseHelper.internalServerError(e);
                    }
                });
    }

    public Mono<ServerResponse> readDriver(ServerRequest request) {
        UUID driverId = UUID.fromString(request.pathVariable("id"));
        try {
            Driver driver = repo.getDriver(driverId);
            return ServiceResponseHelper.successWithBody(driver);
        } catch (TmsException e) {
            if (ErrorCodes.REPO_PROVIDER_ISSUES.getCode() == e.getErrorCode()) {
                return ServiceResponseHelper.notFound("data is not locatable", e);
            }
            return ServiceResponseHelper.internalServerError(e);
        }
    }

    public Mono<ServerResponse> updateDriver(ServerRequest request) {
        return request.bodyToMono(Driver.class)
                .flatMap(driver -> {
                    try {
                        UUID updatedId = repo.editDriver(driver);
                        log.info("updated driver feturing UUID: " + updatedId.toString());
                        return ServiceResponseHelper.successWithBody(driver);
                    } catch (TmsException e) {
                        if (ErrorCodes.REPO_PROVIDER_ISSUES.getCode() == e.getErrorCode()) {
                            return ServiceResponseHelper.badRequest("data supplied face issues", e);
                        }
                        return ServiceResponseHelper.internalServerError(e);
                    }
                });
    }

    public Mono<ServerResponse> deleteDriver(ServerRequest request) {
        UUID driverId = UUID.fromString(request.pathVariable("id"));
        try {
            repo.deleteDriver(driverId);
            return ServerResponse.noContent().build();
        } catch (TmsException e) {
            if (ErrorCodes.REPO_PROVIDER_ISSUES.getCode() == e.getErrorCode()) {
                return ServiceResponseHelper.badRequest("data supplied face issues", e);
            }
            return ServiceResponseHelper.internalServerError(e);
        }
    }
}
