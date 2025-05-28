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

    private static final Pattern NAME_FIELD_MULT_CONSECUTIVE = Pattern.compile("\\.\\.+|&&++");
    private static final Pattern NAME_FIELD_INVALID_SEQUENCES = Pattern.compile("&\\.|\\.&");

    public Customer(final UUID customerId, final UUID tenantId, String name) {
        this(customerId, tenantId);
        this.name = removeMultipleSpaces(name.trim());
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

        if (name.startsWith(".") || name.startsWith("&")) {
            throw new TmsException("Customer name must not start with a dot", ErrorCodes.INVALID_DATA);
        }

        if (NAME_FIELD_INVALID_SEQUENCES.matcher(name).find()) {
            throw new TmsException("Customer name must not contain the sequences '&.' or '.&'", ErrorCodes.INVALID_DATA);
        }

        if (NAME_FIELD_MULT_CONSECUTIVE.matcher(name).find()) {
            throw new TmsException("Customer name must not contain multiple consecutive dots", ErrorCodes.INVALID_DATA);
        }

        for (char c : name.toCharArray()) {
            if (!(Character.isLetterOrDigit(c) || c == ' ' || c == '.' || c == '&')) {
                throw new TmsException("Customer name must be alphanumeric and may contain a few single special characters only", ErrorCodes.INVALID_DATA);
            }
        }
    }
}
