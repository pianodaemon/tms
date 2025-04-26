package com.agnux.haul.core.mgmt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor
@Getter
public class TenantDetailsDto {

    private @NonNull String tenantId;
    private @NonNull String userId;
}
