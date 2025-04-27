package com.agnux.haul.repository.model;

import lombok.Getter;

@Getter
public class Vehicle extends TmsModel {

    public Vehicle(final String vehicleId, final String tenantId) {
        super(vehicleId, tenantId);
    }

    public Vehicle(final String tenantId) {
        this(null, tenantId);
    }
}
