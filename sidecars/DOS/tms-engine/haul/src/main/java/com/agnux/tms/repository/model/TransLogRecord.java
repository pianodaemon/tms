package com.agnux.tms.repository.model;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.Getter;

@Getter
public class TransLogRecord extends TmsBasicModel {

    private final UUID cargoAssignmentId;
    private final DistUnit distUnit;
    private final BigDecimal distScalar;
    private final BigDecimal fuelConsumption;

    public TransLogRecord(UUID transLogRecordId, UUID tenantId, DistUnit distUnit, UUID cargoAssignmentId, BigDecimal distScalar, BigDecimal fuelConsumption) {
        super(transLogRecordId, tenantId);
        this.cargoAssignmentId = cargoAssignmentId;
        this.distUnit = distUnit;
        this.distScalar = distScalar;
        this.fuelConsumption = fuelConsumption;
    }
}
