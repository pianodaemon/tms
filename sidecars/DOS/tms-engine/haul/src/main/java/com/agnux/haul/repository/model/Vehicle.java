package com.agnux.haul.repository.model;

import lombok.Getter;

@Getter
public class Vehicle extends TmsModel {

    private String NumberPlate;
            
    public Vehicle(final String vehicleId, final String tenantId, String NumberPlate) {
        super(vehicleId, tenantId);
    }

    public Vehicle(final String tenantId, String NumberPlate) {
        this(null, tenantId, NumberPlate);
    }
}
