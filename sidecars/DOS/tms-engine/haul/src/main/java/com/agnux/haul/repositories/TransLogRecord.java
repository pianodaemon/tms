package com.agnux.haul.repositories;

import java.math.BigDecimal;

import lombok.Getter;

@Getter
public class TransLogRecord extends TmsModel {

    private DistUnit distUnit;
    private BigDecimal distScalar;
    private BigDecimal fuelConsumption;

    public TransLogRecord(String transLogRecordId, String tenantId, DistUnit distUnit, BigDecimal distScalar, BigDecimal fuelConsumption) {
        super(transLogRecordId, tenantId);
        this.distUnit = distUnit;
        this.distScalar = distScalar;
        this.fuelConsumption = fuelConsumption;
    }

    public TransLogRecord(String tenantId, DistUnit distUnit, BigDecimal distScalar, BigDecimal fuelConsumption) {
        this(null, tenantId, distUnit, distScalar, fuelConsumption);
    }
}
