package com.agnux.tms.repository.model;

import java.util.UUID;
import lombok.Getter;

@Getter
public class CargoAssignment extends TmsBasicModel {

    private final UUID driverId;
    private final UUID vehicleId;

    // Where is the latest/current global location of this assigment
    private double latitudeLocation;
    private double longitudeLocation;

    public CargoAssignment(final UUID cargoAssignmentId, final UUID tenantId, UUID driverId, UUID vehicleId) {
        super(cargoAssignmentId, tenantId);
        this.driverId = driverId;
        this.vehicleId = vehicleId;
    }

    public void setLatitudeLocation(double latitudeLocation) {
        this.latitudeLocation = latitudeLocation;
    }

    public void setLongitudeLocation(double longitudeLocation) {
        this.longitudeLocation = longitudeLocation;
    }
}
