package com.agnux.haul.repository.model;

import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Driver extends TmsBasicModel {

    private String name;
    private String licenseNumber;

    public Driver(final UUID driverId, final UUID tenantId) {
        super(driverId, tenantId);
    }

    public Driver(final UUID tenantId) {
        this(null, tenantId);
    }
}
