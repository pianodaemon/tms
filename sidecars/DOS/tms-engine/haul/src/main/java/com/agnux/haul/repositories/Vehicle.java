package com.agnux.haul.repositories;

import lombok.Getter;

@Getter
public class Vehicle extends TmcModel {

    public Vehicle(final String vehicleId, final String tenantId) {
        super(vehicleId, tenantId);
    }

    public Vehicle(final String tenantId) {
        this(null, tenantId);
    }
}
