package com.agnux.tms.api;

import com.agnux.tms.core.mgmt.HaulMgmt;
import com.agnux.tms.errors.TmsException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/haul")
@RequiredArgsConstructor
public class HaulMgmtController {

    private final HaulMgmt haulMgmt;

    @PostMapping("/assign-trip")
    public Mono<ResponseEntity<UUID>> assignTrip(@RequestBody AssignTripRequest request) {
        return Mono.fromCallable(() -> {
            UUID result = haulMgmt.assignTrip(request.getTenant(), request.getTrip());
            return ResponseEntity.ok(result);
        }).onErrorResume(TmsException.class, ex
                -> Mono.just(ResponseEntity.badRequest().build())
        );
    }

    @PostMapping("/estimate-fuel")
    public Mono<ResponseEntity<String>> estimateFuel(@RequestBody AssignTripRequest request) {
        return Mono.fromCallable(()
                -> haulMgmt.estimateFuel(request.getTenant(), request.getTrip()).toPlainString()
        ).map(ResponseEntity::ok)
                .onErrorResume(TmsException.class, ex
                        -> Mono.just(ResponseEntity.badRequest().body("Error: " + ex.getMessage()))
                );
    }
}
