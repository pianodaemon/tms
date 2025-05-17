package com.agnux.tms.repository.model;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class TransLogRecord extends TmsBasicModel {

    private UUID cargoAssignmentId;
    private DistUnit distUnit;
    private BigDecimal distScalar;
    private BigDecimal fuelConsumption;

    public TransLogRecord(final UUID transLogRecordId, final UUID tenantId,
            final DistUnit distUnit, final UUID cargoAssignmentId,
            final BigDecimal distScalar, final BigDecimal fuelConsumption) {
        this(transLogRecordId, tenantId);
        this.cargoAssignmentId = cargoAssignmentId;
        this.distUnit = distUnit;
        this.distScalar = distScalar;
        this.fuelConsumption = fuelConsumption;
    }

    public TransLogRecord(UUID transLogRecordId, UUID tenantId) {
        super(transLogRecordId, tenantId);
    }
}
