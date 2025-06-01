package com.agnux.tms.repository.model;

import com.agnux.tms.errors.ErrorCodes;
import com.agnux.tms.errors.TmsException;
import com.agnux.tms.reference.qualitative.BoxBrand;
import com.agnux.tms.reference.qualitative.BoxType;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Pattern;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class Box extends TmsBasicModel {

    private String name;
    private BoxType boxType;
    private BoxBrand brand;
    private int numberOfAxis;
    private String numberSerial;
    private String numberPlate;
    private Date numberPlateExpiration;
    private int boxYear;
    private boolean lease;

    private static final Pattern NAME_FIELD_MULTI_CONSECUTIVE = Pattern.compile("\\.\\.+|--++");
    private static final Pattern NAME_FIELD_INVALID_SEQUENCES = Pattern.compile("-\\.|\\.-");
    private static final Pattern NUMBER_PLATE_PATTERN = Pattern.compile("^[A-Za-z0-9]{4,10}$");

    public Box(final UUID boxId, final UUID tenantId,
            String name, final BoxType boxType, BoxBrand brand,
            final int numberOfAxis,  String numberSerial,
            String numberPlate, Date numberPlateExpiration, int boxYear, boolean lease) {
        this(boxId, tenantId);
        this.name = removeMultipleSpaces(name.trim());
        this.boxType = boxType;
        this.brand = brand;
        this.numberOfAxis = numberOfAxis;
        this.numberSerial = removeMultipleSpaces(numberSerial.trim());
        this.numberPlate = removeMultipleSpaces(numberPlate.trim());
        this.numberPlateExpiration = numberPlateExpiration;
        this.boxYear = boxYear;
        this.lease = lease;
    }

    public Box(final UUID boxId, final UUID tenantId) {
        super(boxId, tenantId);
    }

    @Override
    public void validate() throws TmsException {
        super.validate();
        this.validateName();
        this.validateNumSerial();
        this.validateNumberPlate();
        this.validateNumberPlateExpiration();
        this.validateBoxYear();
        this.validateNumberAxis();
    }

    public void validateNumberAxis() throws TmsException {
        if (numberOfAxis < 0) {
            throw new TmsException("Number of axis must not be negative", ErrorCodes.INVALID_DATA);
        }
    }

    public void validateNumSerial() throws TmsException {
        if (numberSerial == null || numberSerial.isBlank()) {
            throw new TmsException("Box serial number must not be null or blank", ErrorCodes.INVALID_DATA);
        }
    }

    public void validateName() throws TmsException {
        if (name == null || name.isBlank()) {
            throw new TmsException("Box name must not be null or blank", ErrorCodes.INVALID_DATA);
        }

        if (name.startsWith(".") || name.startsWith("-")) {
            throw new TmsException("Box name must not start with a dot", ErrorCodes.INVALID_DATA);
        }

        if (NAME_FIELD_INVALID_SEQUENCES.matcher(name).find()) {
            throw new TmsException("Box name must not contain the sequences '-.' or '.-'", ErrorCodes.INVALID_DATA);
        }

        if (NAME_FIELD_MULTI_CONSECUTIVE.matcher(name).find()) {
            throw new TmsException("Box name must not contain multiple consecutive dots/hyphens", ErrorCodes.INVALID_DATA);
        }

        for (char c : name.toCharArray()) {
            if (!(Character.isLetterOrDigit(c) || c == ' ' || c == '.' || c == '-')) {
                throw new TmsException("Box name must be alphanumeric and may contain a few single special characters only", ErrorCodes.INVALID_DATA);
            }
        }
    }

    private void validateBoxYear() throws TmsException {
        if (boxYear < 1970) {
            throw new TmsException("Box year must be no earlier than 1970", ErrorCodes.INVALID_DATA);
        }

        int currentYear = java.time.Year.now().getValue();
        if (boxYear > currentYear + 1) {
            throw new TmsException("Box year cannot be in the far future", ErrorCodes.INVALID_DATA);
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
