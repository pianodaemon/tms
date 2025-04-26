package com.agnux.haul.repositories;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CargoAssignment {

    private String tenantId;
    private Vehicle vehicle;
    private TransLogRecord tlRecord;
}
