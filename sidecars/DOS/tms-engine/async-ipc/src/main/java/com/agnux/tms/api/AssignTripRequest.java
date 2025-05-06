package com.agnux.tms.api;

import com.agnux.tms.core.mgmt.TenantDetailsDto;
import com.agnux.tms.core.mgmt.TripDetailsDto;
import lombok.Data;

@Data
public class AssignTripRequest {
    private TenantDetailsDto tenant;
    private TripDetailsDto trip;
}