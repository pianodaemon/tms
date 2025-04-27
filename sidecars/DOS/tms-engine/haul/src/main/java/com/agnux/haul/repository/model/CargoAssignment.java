package com.agnux.haul.repository.model;

import com.agnux.haul.repository.model.Vehicle;
import lombok.Getter;

@Getter
public class CargoAssignment extends TmsModel {

    private Vehicle vehicle;
    private TransLogRecord tlRecord;

    public CargoAssignment(final String cargoAssignmentId, final String tenantId, Vehicle vehicle, TransLogRecord tlRecord) {
        super(cargoAssignmentId, tenantId);
        this.vehicle = vehicle;
        this.tlRecord = tlRecord;
    }

    public CargoAssignment(String tenantId, Vehicle vehicle, TransLogRecord tlRecord) {
        this(null, tenantId, vehicle, tlRecord);
    }
}
