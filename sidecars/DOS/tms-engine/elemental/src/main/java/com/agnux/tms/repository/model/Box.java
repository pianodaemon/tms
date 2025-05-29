package com.agnux.tms.repository.model;

import com.agnux.tms.errors.ErrorCodes;
import com.agnux.tms.errors.TmsException;
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
    private String numberSerial;
    private String numberPlate;
    private Date numberPlateExpiration;
    private int boxYear;
    private boolean lease;

    private static final Pattern NAME_FIELD_MULT_CONSECUTIVE = Pattern.compile("\\.\\.+|--++");
    private static final Pattern NAME_FIELD_INVALID_SEQUENCES = Pattern.compile("-\\.|\\.-");

    public Box(final UUID boxId, final UUID tenantId, String name, String numberSerial, String numberPlate, Date numberPlateExpiration, int boxYear, boolean lease) {
        this(boxId, tenantId);
        this.name = removeMultipleSpaces(name.trim());
        this.numberSerial = numberSerial.trim();
        this.numberPlate = numberPlate.trim();
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

        if (name == null || name.isBlank()) {
            throw new TmsException("Box name must not be null or blank", ErrorCodes.INVALID_DATA);
        }

        if (name.startsWith(".") || name.startsWith("-")) {
            throw new TmsException("Box name must not start with a dot", ErrorCodes.INVALID_DATA);
        }

        if (NAME_FIELD_INVALID_SEQUENCES.matcher(name).find()) {
            throw new TmsException("Box name must not contain the sequences '&.' or '.&'", ErrorCodes.INVALID_DATA);
        }

        if (NAME_FIELD_MULT_CONSECUTIVE.matcher(name).find()) {
            throw new TmsException("Box name must not contain multiple consecutive dots", ErrorCodes.INVALID_DATA);
        }

        for (char c : name.toCharArray()) {
            if (!(Character.isLetterOrDigit(c) || c == ' ' || c == '.' || c == '-')) {
                throw new TmsException("Box name must be alphanumeric and may contain a few single special characters only", ErrorCodes.INVALID_DATA);
            }
        }
    }
}
