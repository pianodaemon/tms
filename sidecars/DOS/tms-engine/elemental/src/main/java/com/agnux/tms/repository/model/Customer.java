package com.agnux.tms.repository.model;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class Customer extends TmsBasicModel {

    private String name;

    public Customer(final UUID customerId, final UUID tenantId, String name) {
        this(customerId, tenantId);
        this.name = name;
    }

    public Customer(final UUID customerId, final UUID tenantId) {
        super(customerId, tenantId);
    }
}
