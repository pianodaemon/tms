package com.agnux.haul.repository.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Driver extends TmsBasicModel {

    private String name;
    private String licenseNumber;
    
    public Driver(final String DriverId, final String tenantId) {
        super(DriverId, tenantId);
    }

    public Driver(final String tenantId) {
        this(null, tenantId);
    }
}
