package com.agnux.tms.api;

import com.agnux.tms.core.mgmt.HaulMgmt;
import com.agnux.tms.core.mgmt.TenantDetailsDto;
import com.agnux.tms.core.mgmt.TripDetailsDto;
import com.agnux.tms.errors.TmsException;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Log4j2
public class HaulMgmtHandler {

    private final HaulMgmt haulMgmt;

    public Mono<ServerResponse> assignTrip(ServerRequest request) {
        UUID tenantId = UUID.fromString(request.pathVariable("tenantId"));
        String userId = request.pathVariable("userId");
        TenantDetailsDto tenant = new TenantDetailsDto(tenantId, userId);

        return request.bodyToMono(TripDetailsDto.class)
                .flatMap(tripDetails
                        -> Mono.fromCallable(() -> haulMgmt.assignTrip(tenant, tripDetails))
                        .flatMap(cargoId -> ServerResponse.ok().bodyValue(cargoId.toString()))
                        .onErrorResume(TmsException.class, e -> ServiceResponseHelper.badRequest("Assignment failed", e))
                );
    }

    public Mono<ServerResponse> estimateFuel(ServerRequest request) {
        UUID tenantId = UUID.fromString(request.pathVariable("tenantId"));
        String userId = request.pathVariable("userId");
        UUID vehicleId = UUID.fromString(request.queryParam("vehicleId").orElseThrow());
        UUID agreementId = UUID.fromString(request.queryParam("agreementId").orElseThrow());
        UUID driverId = UUID.fromString(request.queryParam("driverId").orElseThrow());
        TenantDetailsDto tenant = new TenantDetailsDto(tenantId, userId);

        return Mono.fromCallable(() -> haulMgmt.estimateFuel(tenant, vehicleId, agreementId).toPlainString())
                .flatMap(fuel -> ServerResponse.ok().bodyValue(fuel))
                .onErrorResume(TmsException.class, e -> ServiceResponseHelper.badRequest("Estimation failed", e));
    }
}
