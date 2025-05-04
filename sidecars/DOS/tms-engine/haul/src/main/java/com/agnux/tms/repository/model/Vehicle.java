package com.agnux.tms.repository.model;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Vehicle extends TmsBasicModel {

    private String numberPlate;
    private VehicleType vehicleType;
    public int vehicleYear;

    // El rendimiento es una media de los históricos
    // Algunos vehiculos caracterizan una interface electronica
    // para obtener rendimiento directamente del motor
    // El rendimiento es una media de los históricos
    private DistUnit perfDistUnit;
    private VolUnit perfVolUnit;
    private BigDecimal perfScalar;

    public Vehicle(final UUID vehicleId, final UUID tenantId, String numberPlate, VehicleType vehicleType, int vehicleYear) {
        super(vehicleId, tenantId);
        this.numberPlate = numberPlate;
        this.vehicleType = vehicleType;
        this.vehicleYear = vehicleYear;
    }
}
