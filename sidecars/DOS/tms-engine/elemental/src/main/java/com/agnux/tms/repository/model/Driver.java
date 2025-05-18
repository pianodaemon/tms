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
    private String licenseNumber;

    public Driver(final UUID driverId, final UUID tenantId,
            final String name, final String firstSurname, final String licenseNumber) {
        this(driverId, tenantId);
        this.name = name;
        this.firstSurname = firstSurname;
        this.licenseNumber = licenseNumber;
    }

    public Driver(final UUID driverId, final UUID tenantId) {
        super(driverId, tenantId);
    }
}
