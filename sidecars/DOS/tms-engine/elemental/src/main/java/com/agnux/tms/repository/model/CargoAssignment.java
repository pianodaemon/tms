package com.agnux.tms.repository.model;

import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CargoAssignment extends TmsBasicModel {

    private UUID driverId;
    private UUID vehicleId;

    // Where is the latest/current global location of this assigment
    private double latitudeLocation;
    private double longitudeLocation;

    public CargoAssignment(final UUID cargoAssignmentId, final UUID tenantId,
            UUID driverId, UUID vehicleId,
            double latitudeLocation, double longitudeLocation) {
        this(cargoAssignmentId, tenantId);
        this.driverId = driverId;
        this.vehicleId = vehicleId;
        this.latitudeLocation = latitudeLocation;
        this.longitudeLocation = longitudeLocation;
    }

    public CargoAssignment(final UUID cargoAssignmentId, final UUID tenantId) {
        super(cargoAssignmentId, tenantId);
    }
}
