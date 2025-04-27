package com.agnux.haul.repository.model;

import lombok.Getter;

@Getter
public class Vehicle extends TmsModel {

    private String numberPlate;
    private VehicleType vehicleType;

    public Vehicle(final String vehicleId, final String tenantId, String numberPlate, VehicleType vehicleType) {
        super(vehicleId, tenantId);
        this.numberPlate = numberPlate;
        this.vehicleType = vehicleType;
    }

    public Vehicle(final String tenantId, String numberPlate, VehicleType vehicleType) {
        this(null, tenantId, numberPlate, vehicleType);
    }
}
