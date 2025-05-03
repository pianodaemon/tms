package com.agnux.haul.repository.model;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.Getter;

@Getter
public class TransLogRecord extends TmsBasicModel {

    private DistUnit distUnit;
    private BigDecimal distScalar;
    private BigDecimal fuelConsumption;

    public TransLogRecord(UUID transLogRecordId, UUID tenantId, DistUnit distUnit, BigDecimal distScalar, BigDecimal fuelConsumption) {
        super(transLogRecordId, tenantId);
        this.distUnit = distUnit;
        this.distScalar = distScalar;
        this.fuelConsumption = fuelConsumption;
    }

    public TransLogRecord(UUID tenantId, DistUnit distUnit, BigDecimal distScalar, BigDecimal fuelConsumption) {
        this(null, tenantId, distUnit, distScalar, fuelConsumption);
    }
}
