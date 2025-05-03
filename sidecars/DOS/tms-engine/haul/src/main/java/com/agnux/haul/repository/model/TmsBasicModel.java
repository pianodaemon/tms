package com.agnux.haul.repository.model;

import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TmsBasicModel {

    protected UUID Id;
    protected UUID tenantId;

    public Optional<UUID> getId() {
        return Optional.ofNullable(this.Id);
    }
    
    public UUID getTenantId(){
        return this.tenantId;
    }
}
