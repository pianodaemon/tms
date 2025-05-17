package com.agnux.tms.repository.model;

import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class TmsBasicModel {

    protected UUID id;
    protected UUID tenantId;

    public Optional<UUID> getId() {
        return Optional.ofNullable(this.id);
    }
    
    public UUID getTenantId(){
        return this.tenantId;
    }
}
