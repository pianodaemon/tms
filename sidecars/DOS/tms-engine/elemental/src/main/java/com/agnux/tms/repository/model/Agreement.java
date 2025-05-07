package com.agnux.tms.repository.model;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Agreement extends TmsBasicModel {

    /*
    - No pueden existir dos Convenios con las mismas coordenadas polares (Origen y destino) para un mismo cliente
    - Dicho de otra manera, no puede haber dos convenios para un mismo cliente que tengan la misma ruta
     */
    private UUID customerId;
    private String receiver;     // AKA el destinatario
    private double latitudeOrigin;
    private double longitudeOrigin;
    private double latitudeDestiny;
    private double longitudeDestiny;

    private DistUnit distUnit;
    private BigDecimal distScalar;

    public Agreement(UUID agreementId, UUID tenantId,
            UUID customerId, final String receiver,
            double latitudeOrigin, double longitudeOrigin,
            double latitudeDestiny, double longitudeDestiny,
            DistUnit distUnit, BigDecimal distScalar) {
        this(agreementId, tenantId);
        this.customerId = customerId;
        this.receiver = receiver;
        this.latitudeOrigin = latitudeOrigin;
        this.longitudeOrigin = longitudeOrigin;
        this.latitudeDestiny = latitudeDestiny;
        this.longitudeDestiny = longitudeDestiny;
        this.distUnit = distUnit;
        this.distScalar = distScalar;
    }

    public Agreement(UUID agreementId, UUID tenantId) {
        super(agreementId, tenantId);
    }
}
