package com.agnux.haul.repository.model;

import lombok.Getter;

@Getter
public class Customer extends TmsModel {

    public Customer(final String customerId, final String tenantId) {
        super(customerId, tenantId);
    }

    public Customer(final String tenantId) {
        this(null, tenantId);
    }
}
