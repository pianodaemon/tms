package com.agnux.tms.api.dto;

import com.agnux.tms.repository.model.DistUnit;
import java.math.BigDecimal;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AgreementDto {

    private UUID customerId;
    private String receiver;     // AKA el destinatario
    private double latitudeOrigin;
    private double longitudeOrigin;
    private double latitudeDestiny;
    private double longitudeDestiny;

    private DistUnit distUnit;
    private BigDecimal distScalar;
}
