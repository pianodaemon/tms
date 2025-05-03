package com.agnux.haul.repository.model;

import java.util.UUID;
import lombok.Getter;

@Getter
public class Customer extends TmsBasicModel {

    public Customer(final UUID customerId, final String tenantId) {
        super(customerId, tenantId);
    }

    public Customer(final String tenantId) {
        this(null, tenantId);
    }
}
