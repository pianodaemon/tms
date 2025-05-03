package com.agnux.haul.repository.model;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TmsBasicModel {
    protected UUID Id;
    protected UUID tenantId;
}
