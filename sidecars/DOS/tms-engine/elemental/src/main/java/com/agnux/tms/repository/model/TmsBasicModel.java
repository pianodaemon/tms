package com.agnux.tms.repository.model;

import com.agnux.tms.errors.ErrorCodes;
import com.agnux.tms.errors.TmsException;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TmsBasicModel {

    protected UUID id;
    protected UUID tenantId;

    public Optional<UUID> getId() {
        return Optional.ofNullable(this.id);
    }

    public UUID getTenantId() {
        return this.tenantId;
    }

    public void validate() throws TmsException {
        if (tenantId == null) {
            throw new TmsException("tenantId must not be null", ErrorCodes.INVALID_DATA);
        }

        if (!getId().isPresent()) {
            throw new TmsException("id must not be null", ErrorCodes.INVALID_DATA);
        }
    }
}
