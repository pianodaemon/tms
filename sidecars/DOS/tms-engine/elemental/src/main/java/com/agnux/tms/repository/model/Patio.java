package com.agnux.tms.repository.model;

import java.util.UUID;
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

    public Patio(final UUID patioId, final UUID tenantId, String name, double latitudeLocation, double longitudeLocation) {
        this(patioId, tenantId);
        this.name = name;
        this.latitudeLocation = latitudeLocation;
        this.longitudeLocation = longitudeLocation;
    }

    public Patio(final UUID patioId, final UUID tenantId) {
        super(patioId, tenantId);
    }
}
