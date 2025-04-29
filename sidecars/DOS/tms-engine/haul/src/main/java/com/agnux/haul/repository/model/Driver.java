package com.agnux.haul.repository.model;

import lombok.Getter;

@Getter
public class Driver extends TmsBasicModel {

    public Driver(final String DriverId, final String tenantId) {
        super(DriverId, tenantId);
    }

    public Driver(final String tenantId) {
        this(null, tenantId);
    }
}
