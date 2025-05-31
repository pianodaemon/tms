package com.agnux.tms.repository.model;

import com.agnux.tms.errors.ErrorCodes;
import com.agnux.tms.errors.TmsException;
import com.agnux.tms.reference.qualitative.VehicleColor;
import com.agnux.tms.reference.qualitative.VehicleType;
import com.agnux.tms.reference.quantitative.DistUnit;
import com.agnux.tms.reference.quantitative.VolUnit;
import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Pattern;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class Vehicle extends TmsBasicModel {

    private String numberPlate;
    private Date numberPlateExpiration;
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

    private static final Pattern NUMBER_PLATE_PATTERN = Pattern.compile("^[A-Za-z0-9]{4,10}$");

    public Vehicle(final UUID vehicleId, final UUID tenantId,
            final String numberPlate, final Date numberPlateExpiration, final String numberSerial,
            final VehicleType vehicleType, final VehicleColor vehicleColor,
            final int vehicleYear, final String federalConf,
            DistUnit perfDistUnit, VolUnit perfVolUnit, BigDecimal perfScalar) {
        this(vehicleId, tenantId);
        this.numberPlate = numberPlate;
        this.numberPlateExpiration = numberPlateExpiration;
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

    @Override
    public void validate() throws TmsException {
        super.validate();
        this.validateNumberPlate();
        this.validateNumberPlateExpiration();
        this.validateVehicleYear();
    }

    private void validateVehicleYear() throws TmsException {
        if (vehicleYear < 1970) {
            throw new TmsException("Vehicle year must be no earlier than 1970", ErrorCodes.INVALID_DATA);
        }

        int currentYear = java.time.Year.now().getValue();
        if (vehicleYear > currentYear + 1) {
            throw new TmsException("Vehicle year cannot be in the far future", ErrorCodes.INVALID_DATA);
        }
    }

    private void validateNumberPlate() throws TmsException {
        if (numberPlate == null) {
            throw new TmsException("Number plate must not be null", ErrorCodes.INVALID_DATA);
        }

        if (!NUMBER_PLATE_PATTERN.matcher(numberPlate).matches()) {
            throw new TmsException("Number plate must be greater than 3 and less than 11 alphanumeric characters with no spaces", ErrorCodes.INVALID_DATA);
        }
    }

    private void validateNumberPlateExpiration() throws TmsException {
        if (numberPlateExpiration == null) {
            throw new TmsException("Number plate expiration date must not be null", ErrorCodes.INVALID_DATA);
        }

        Date today = new Date();
        if (!numberPlateExpiration.after(today)) {
            throw new TmsException("Number plate expiration date must be in the future", ErrorCodes.INVALID_DATA);
        }
    }
}
