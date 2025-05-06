package com.agnux.tms.api;

import com.agnux.tms.core.mgmt.HaulMgmt;
import com.agnux.tms.core.mgmt.TenantDetailsDto;
import com.agnux.tms.core.mgmt.TripDetailsDto;
import com.agnux.tms.errors.TmsException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/haul/{tenantId}/{userId}")
@RequiredArgsConstructor
public class HaulMgmtController {

    private final HaulMgmt haulMgmt;

    @PostMapping("/assign-trip")
    public Mono<ResponseEntity<String>> assignTrip(
            @PathVariable UUID tenantId,
            @PathVariable String userId,
            @RequestBody TripDetailsDto tripDetails
    ) {
        TenantDetailsDto tenantDetails = new TenantDetailsDto(tenantId, userId);
        return Mono.fromCallable(() -> haulMgmt.assignTrip(tenantDetails, tripDetails))
                .map(cargoId -> ResponseEntity.ok(cargoId.toString()))
                .onErrorResume(TmsException.class,
                        ex -> Mono.just(ResponseEntity.badRequest().body("Error: " + ex.getMessage())));
    }

    @GetMapping("/estimate-fuel")
    public Mono<ResponseEntity<String>> estimateFuel(
            @PathVariable UUID tenantId,
            @PathVariable String userId,
            @RequestParam UUID vehicleId,
            @RequestParam UUID agreementId,
            @RequestParam UUID driverId
    ) {
        TenantDetailsDto tenant = new TenantDetailsDto(tenantId, userId);
        TripDetailsDto trip = new TripDetailsDto(vehicleId, agreementId, driverId);

        return Mono.fromCallable(()
                -> haulMgmt.estimateFuel(tenant, vehicleId, agreementId).toPlainString()
        ).map(ResponseEntity::ok)
                .onErrorResume(TmsException.class, ex
                        -> Mono.just(ResponseEntity.badRequest().body("Error: " + ex.getMessage()))
                );
    }

}
