package com.agnux.tms.core.mgmt;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor
@Getter
public class TripDetailsDto {

    private @NonNull
    UUID vehicleId;

    private @NonNull
    UUID agreementId;

    private @NonNull
    UUID driverId;
}
