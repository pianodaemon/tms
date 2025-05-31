package com.agnux.tms.api.dto;

import com.agnux.tms.reference.quantitative.DistUnit;
import com.agnux.tms.reference.qualitative.VehicleColor;
import com.agnux.tms.reference.qualitative.VehicleType;
import com.agnux.tms.reference.quantitative.VolUnit;
import java.math.BigDecimal;
import java.util.Date;
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
    private Date numberPlateExpiration;
    private String numberSerial;
    private int numberOfAxis;
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
