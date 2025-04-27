package com.agnux.haul.repositories;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CargoAssignment {

    private String Id;
    private String tenantId;

    private Vehicle vehicle;
    private TransLogRecord tlRecord;

    public CargoAssignment(String tenantId, Vehicle vehicle, TransLogRecord tlRecord) {
        this(null, tenantId, vehicle, tlRecord);
    }
}
