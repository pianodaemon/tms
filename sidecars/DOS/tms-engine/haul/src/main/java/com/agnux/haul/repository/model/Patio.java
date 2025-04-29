package com.agnux.haul.repository.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Patio extends TmsBasicModel {

    // Patio is featuring a geolocalization as well
    private double latitudeLocation;
    private double longitudeLocation;

    public Patio(final String patioId, final String tenantId) {
        super(patioId, tenantId);
    }

    public Patio(final String tenantId) {
        this(null, tenantId);
    }
}
