package com.agnux.tms.repository.model;

import java.util.UUID;

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

    public Driver(final UUID driverId, final UUID tenantId,
            final String name, final String firstSurname, final String secondSurname, final String licenseNumber) {
        this(driverId, tenantId);
        this.name = name;
        this.firstSurname = firstSurname;
        this.secondSurname = secondSurname;
        this.licenseNumber = licenseNumber;
    }

    public Driver(final UUID driverId, final UUID tenantId) {
        super(driverId, tenantId);
    }
}
