package com.agnux.tms.repository.model;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class Vehicle extends TmsBasicModel {

    private String numberPlate;
    private String numberSerial;
    private VehicleType vehicleType;
    private VehicleColor vehicleColor;
    private int vehicleYear;
    private String federalConf;

    // El rendimiento es una media de los históricos
    // Algunos vehiculos caracterizan una interface electronica
    // para obtener rendimiento directamente del motor
    // El rendimiento es una media de los históricos
    private DistUnit perfDistUnit;
    private VolUnit perfVolUnit;
    private BigDecimal perfScalar;

    public Vehicle(final UUID vehicleId, final UUID tenantId,
            final String numberPlate, final String numberSerial,
            final VehicleType vehicleType, final VehicleColor vehicleColor,
            final int vehicleYear, final String federalConf,
            DistUnit perfDistUnit, VolUnit perfVolUnit, BigDecimal perfScalar) {
        this(vehicleId, tenantId);
        this.numberPlate = numberPlate;
        this.numberSerial = numberSerial;
        this.vehicleType = vehicleType;
        this.vehicleColor = vehicleColor;
        this.vehicleYear = vehicleYear;
        this.federalConf = federalConf;
        this.perfScalar = perfScalar;
        this.perfDistUnit = perfDistUnit;
        this.perfVolUnit = perfVolUnit;
    }

    public Vehicle(final UUID vehicleId, final UUID tenantId) {
        super(vehicleId, tenantId);
    }
}
