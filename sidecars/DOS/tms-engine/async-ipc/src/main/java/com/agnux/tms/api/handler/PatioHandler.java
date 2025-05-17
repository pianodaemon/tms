package com.agnux.tms.api.handler;

import com.agnux.tms.errors.ErrorCodes;
import java.util.UUID;

import com.agnux.tms.repository.BasicRepoImpl;
import com.agnux.tms.repository.model.Patio;
import com.agnux.tms.errors.TmsException;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Component
@RequiredArgsConstructor
@Log4j2
public class PatioHandler {

    private final BasicRepoImpl repo;

    public Mono<ServerResponse> createPatio(ServerRequest request) {
        return request.bodyToMono(Patio.class)
                .flatMap(patio -> {
                    try {
                        UUID newId = repo.createPatio(patio);
                        log.info("created patio featuring UUID: " + newId.toString());
                        patio.setId(newId);
                        return ServiceResponseHelper.successWithBody(patio);
                    } catch (TmsException e) {
                        if (ErrorCodes.REPO_PROVIDER_ISSUES.getCode() == e.getErrorCode()) {
                            return ServiceResponseHelper.badRequest("data supplied face issues", e);
                        }
                        return ServiceResponseHelper.internalServerError(e);
                    }
                });
    }

    public Mono<ServerResponse> readPatio(ServerRequest request) {
        UUID patioId = UUID.fromString(request.pathVariable("id"));
        try {
            Patio patio = repo.getPatio(patioId);
            return ServiceResponseHelper.successWithBody(patio);
        } catch (TmsException e) {
            if (ErrorCodes.REPO_PROVIDER_ISSUES.getCode() == e.getErrorCode()) {
                return ServiceResponseHelper.notFound("data is not locatable", e);
            }
            return ServiceResponseHelper.internalServerError(e);
        }
    }

    public Mono<ServerResponse> updatePatio(ServerRequest request) {
        return request.bodyToMono(Patio.class)
                .flatMap(patio -> {
                    try {
                        UUID updatedId = repo.editPatio(patio);
                        log.info("updated patio feturing UUID: " + updatedId.toString());
                        return ServiceResponseHelper.successWithBody(patio);
                    } catch (TmsException e) {
                        if (ErrorCodes.REPO_PROVIDER_ISSUES.getCode() == e.getErrorCode()) {
                            return ServiceResponseHelper.badRequest("data supplied face issues", e);
                        }
                        return ServiceResponseHelper.internalServerError(e);
                    }
                });
    }

    public Mono<ServerResponse> deletePatio(ServerRequest request) {
        UUID patioId = UUID.fromString(request.pathVariable("id"));
        try {
            repo.deletePatio(patioId);
            return ServerResponse.noContent().build();
        } catch (TmsException e) {
            if (ErrorCodes.REPO_PROVIDER_ISSUES.getCode() == e.getErrorCode()) {
                return ServiceResponseHelper.badRequest("data supplied face issues", e);
            }
            return ServiceResponseHelper.internalServerError(e);
        }
    }
}
