package com.agnux.haul.core.mgmt;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class TripDetailsDto {

    private int origin;
    private int destiny;
    private String distUnit;
    private BigDecimal distScalar;
}
