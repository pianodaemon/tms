package com.agnux.haul.repositories;

import java.math.BigDecimal;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Route {

    public String tenantId;
    public BigDecimal fuelConsumption;
}
