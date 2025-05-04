package com.agnux.haul.repository.model;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.Getter;

@Getter
public class TransLogRecord extends TmsBasicModel {

    private final UUID cargoId;
    private final DistUnit distUnit;
    private final BigDecimal distScalar;
    private final BigDecimal fuelConsumption;

    public TransLogRecord(UUID transLogRecordId, UUID tenantId, DistUnit distUnit, UUID cargoId, BigDecimal distScalar, BigDecimal fuelConsumption) {
        super(transLogRecordId, tenantId);
        this.cargoId = cargoId;
        this.distUnit = distUnit;
        this.distScalar = distScalar;
        this.fuelConsumption = fuelConsumption;
    }
}
