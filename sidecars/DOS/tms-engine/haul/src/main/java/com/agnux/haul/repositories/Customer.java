package com.agnux.haul.repositories;

import lombok.Getter;

@Getter
public class Customer extends TmcModel {

    public Customer(final String customerId, final String tenantId) {
        super(customerId, tenantId);
    }

    public Customer(final String tenantId) {
        this(null, tenantId);
    }
}
