package com.agnux.tms.repository.model;

import com.agnux.tms.errors.ErrorCodes;
import com.agnux.tms.errors.TmsException;
import java.util.UUID;
import java.util.regex.Pattern;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class Customer extends TmsBasicModel {

    private String name;

    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-z0-9](?:[A-Za-z0-9. ]*[A-Za-z0-9.])?$");

    public Customer(final UUID customerId, final UUID tenantId, String name) {
        this(customerId, tenantId);
        this.name = name;
    }

    public Customer(final UUID customerId, final UUID tenantId) {
        super(customerId, tenantId);
    }

    @Override
    public void validate() throws TmsException {
        super.validate();

        if (name == null || name.isBlank()) {
            throw new TmsException("Customer name must not be null or blank", ErrorCodes.INVALID_DATA);
        }

        if (name.contains("  ")) {
            throw new TmsException("Customer name must not contain multiple consecutive spaces", ErrorCodes.INVALID_DATA);
        }

        if (!NAME_PATTERN.matcher(name).matches()) {
            throw new TmsException("Customer name must be alphanumeric and may contain single spaces and dots, but must not start with a dot", ErrorCodes.INVALID_DATA);
        }
    }
}
