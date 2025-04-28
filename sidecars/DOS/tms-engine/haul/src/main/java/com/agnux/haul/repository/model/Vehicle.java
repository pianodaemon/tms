package com.agnux.haul.repository.model;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Vehicle extends TmsModel {

    private String numberPlate;
    private VehicleType vehicleType;

    // El rendimiento es una media de los históricos
    // Algunos vehiculos caracterizan una interface electronica
    // para obtener rendimiento directamente del motor
    // El rendimiento es una media de los históricos
    private DistUnit performanceUnit;
    private BigDecimal performanceScalar;

    public Vehicle(final String vehicleId, final String tenantId, String numberPlate, VehicleType vehicleType) {
        super(vehicleId, tenantId);
        this.numberPlate = numberPlate;
        this.vehicleType = vehicleType;
    }

    public Vehicle(final String tenantId, String numberPlate, VehicleType vehicleType) {
        this(null, tenantId, numberPlate, vehicleType);
    }
}
