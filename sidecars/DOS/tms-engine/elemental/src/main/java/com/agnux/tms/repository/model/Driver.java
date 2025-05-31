package com.agnux.tms.repository.model;

import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.agnux.tms.errors.ErrorCodes;
import com.agnux.tms.errors.TmsException;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class Driver extends TmsBasicModel {

    private String name;
    private String firstSurname;
    private String secondSurname;
    private String licenseNumber;

    final static Pattern PATTERN_ANY_PERSON_NAME = Pattern.compile("^([A-Z][A-Za-z]+(?:[-'][A-Z][a-z]+)?)(\\s[A-Z][a-z]+(?:[-'][A-Z][a-z]+)?)*$");

    public Driver(final UUID driverId, final UUID tenantId,
            final String name, final String firstSurname, final String secondSurname, final String licenseNumber) {
        this(driverId, tenantId);
        this.name = name;
        this.firstSurname = firstSurname.trim();
        this.secondSurname = secondSurname.trim();
        this.licenseNumber = licenseNumber.trim();
    }

    public Driver(final UUID driverId, final UUID tenantId) {
        super(driverId, tenantId);
    }

    @Override
    public void validate() throws TmsException {
        super.validate();
        this.validateNames();
    }

    public void validateNames() throws TmsException {
        String[][] fieldVals = {
                {"name", this.name},
                {"first surname", this.firstSurname},
                {"second surname", this.secondSurname}
        };

        for (String[] field : fieldVals) {
            Matcher matcher = PATTERN_ANY_PERSON_NAME.matcher(field[1]);
            if (!matcher.matches()) {
                throw new TmsException("Invalid " + field[0], ErrorCodes.INVALID_DATA);
            }
        }
    }
}
