package com.agnux.haul.repositories;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Vehicle {

    private String id;
    private String tenantId;

    public Vehicle(final String tenantId) {
        this(null, tenantId);
    }
}
