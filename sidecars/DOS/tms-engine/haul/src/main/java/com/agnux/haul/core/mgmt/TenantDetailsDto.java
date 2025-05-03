package com.agnux.haul.core.mgmt;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor
@Getter
public class TenantDetailsDto {

    private @NonNull UUID tenantId;
    private @NonNull String userId;
}
