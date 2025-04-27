package com.agnux.haul.repositories;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Agreement {

    private String Id;
    private String tenantId;
    private String customerId;
    private double latitudeOrigin;
    private double longitudeOrigin;
    private double latitudeDestiny;
    private double longitudeDestiny;
    private DistUnit distUnit;
    private BigDecimal distScalar;

    public Agreement(String tenantId, String customerId, double latitudeOrigin, double longitudeOrigin, double latitudeDestiny, double longitudeDestiny, DistUnit distUnit, BigDecimal distScalar) {
        this(null, tenantId, customerId, latitudeOrigin, longitudeOrigin, latitudeDestiny, longitudeDestiny, distUnit, distScalar);
    }
}
