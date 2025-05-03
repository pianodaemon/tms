package com.agnux.haul.repository.model;

import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Customer extends TmsBasicModel {

    private String name;
    
    public Customer(final UUID customerId, final UUID tenantId, String name) {
        super(customerId, tenantId);
        this.name = name;
    }
}
