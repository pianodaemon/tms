package com.agnux.tms.repository.model;

import java.util.UUID;
import java.util.regex.Pattern;

import com.agnux.tms.errors.ErrorCodes;
import com.agnux.tms.errors.TmsException;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class Patio extends TmsBasicModel {

    private String name;

    // Patio is featuring a geolocalization as well
    private double latitudeLocation;
    private double longitudeLocation;

    private static final Pattern NAME_FIELD_MULTI_CONSECUTIVE = Pattern.compile("\\.\\.+|--++");
    private static final Pattern NAME_FIELD_INVALID_SEQUENCES = Pattern.compile("-\\.|\\.-");

    public Patio(final UUID patioId, final UUID tenantId, String name, double latitudeLocation, double longitudeLocation) {
        this(patioId, tenantId);
        this.name = removeMultipleSpaces(name.trim());
        this.latitudeLocation = latitudeLocation;
        this.longitudeLocation = longitudeLocation;
    }

    public Patio(final UUID patioId, final UUID tenantId) {
        super(patioId, tenantId);
    }

    @Override
    public void validate() throws TmsException {
        super.validate();
        this.validateName();
    }

    public void validateName() throws TmsException {
        if (name == null || name.isBlank()) {
            throw new TmsException("Patio name must not be null or blank", ErrorCodes.INVALID_DATA);
        }

        if (name.startsWith(".") || name.startsWith("-")) {
            throw new TmsException("Patio name must not start with a dot", ErrorCodes.INVALID_DATA);
        }

        if (NAME_FIELD_INVALID_SEQUENCES.matcher(name).find()) {
            throw new TmsException("Patio name must not contain the sequences '-.' or '.-'", ErrorCodes.INVALID_DATA);
        }

        if (NAME_FIELD_MULTI_CONSECUTIVE.matcher(name).find()) {
            throw new TmsException("Patio name must not contain multiple consecutive dots/hyphens", ErrorCodes.INVALID_DATA);
        }

        for (char c : name.toCharArray()) {
            if (!(Character.isLetterOrDigit(c) || c == ' ' || c == '.' || c == '-')) {
                throw new TmsException("Patio name must be alphanumeric and may contain a few single special characters only", ErrorCodes.INVALID_DATA);
            }
        }
    }
}
