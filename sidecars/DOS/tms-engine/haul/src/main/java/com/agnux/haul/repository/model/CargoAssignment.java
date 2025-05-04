package com.agnux.haul.repository.model;

import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CargoAssignment extends TmsBasicModel {

    private Driver driver;
    private Vehicle vehicle;

    // Where is the latest/current global location of this assigment
    private double latitudeLocation;
    private double longitudeLocation;

    public CargoAssignment(final UUID cargoAssignmentId, final UUID tenantId, Vehicle vehicle) {
        super(cargoAssignmentId, tenantId);
        this.vehicle = vehicle;

    }
}
