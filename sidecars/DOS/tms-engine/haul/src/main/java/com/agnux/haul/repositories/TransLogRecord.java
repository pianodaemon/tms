package com.agnux.haul.repositories;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class TransLogRecord {

    private String id;
    private String distUnit;
    private BigDecimal distScalar;
    private BigDecimal fuelConsumption;

    public TransLogRecord(String distUnit, BigDecimal distScalar, BigDecimal fuelConsumption) {
        this(null, distUnit, distScalar, fuelConsumption);
    }
}
