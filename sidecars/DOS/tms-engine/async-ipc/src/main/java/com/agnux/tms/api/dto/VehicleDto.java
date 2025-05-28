package com.agnux.tms.api.dto;

import com.agnux.tms.repository.model.DistUnit;
import com.agnux.tms.repository.model.VehicleColor;
import com.agnux.tms.repository.model.VehicleType;
import com.agnux.tms.repository.model.VolUnit;
import java.math.BigDecimal;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VehicleDto {

    private UUID id;
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
}
