package com.agnux.haul.core.mgmt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor
@Getter
public class TripDetailsDto {

    private @NonNull
    String vehicleId;

    private @NonNull
    String agreement;
}
