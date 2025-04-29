package com.agnux.haul.repository.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CargoAssignment extends TmsBasicModel {

    private Driver driver;
    private Vehicle vehicle;
    private TransLogRecord tlRecord;

    // Where is the current global location of this assigment
    private double latitudeLocation;
    private double longitudeLocation;

    public CargoAssignment(final String cargoAssignmentId, final String tenantId, Vehicle vehicle, TransLogRecord tlRecord) {
        super(cargoAssignmentId, tenantId);
        this.vehicle = vehicle;
        this.tlRecord = tlRecord;
    }

    public CargoAssignment(String tenantId, Vehicle vehicle, TransLogRecord tlRecord) {
        this(null, tenantId, vehicle, tlRecord);
    }
}
