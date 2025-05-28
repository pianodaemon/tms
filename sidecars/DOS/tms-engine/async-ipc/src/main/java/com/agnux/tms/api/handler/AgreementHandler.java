package com.agnux.tms.api.handler;

import com.agnux.tms.api.dto.AgreementDto;
import com.agnux.tms.api.service.AgreementService;
import com.agnux.tms.errors.TmsException;
import com.agnux.tms.repository.model.Agreement;

import java.util.UUID;

import org.springframework.stereotype.Component;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class AgreementHandler extends ScaffoldHandler<Agreement, AgreementDto> {

    public AgreementHandler(AgreementService service) {
        super(service);
    }

    @Override
    protected Agreement entMapper(AgreementDto dto, UUID tenantId) throws TmsException {
        UUID id = dto.getId();
        final Agreement ent = new Agreement(
                id,
                tenantId,
                dto.getCustomerId(), dto.getReceiver(),
                dto.getLatitudeOrigin(),
                dto.getLongitudeOrigin(),
                dto.getLatitudeDestiny(),
                dto.getLongitudeDestiny(),
                dto.getDistUnit(),
                dto.getDistScalar()
        );

        return ent;
    }

    @Override
    protected AgreementDto dtoMapper(Agreement ent) {
        UUID id = ent.getId().orElseThrow();
        return new AgreementDto(
                id,
                ent.getCustomerId(),
                ent.getReceiver(),
                ent.getLatitudeOrigin(),
                ent.getLongitudeOrigin(),
                ent.getLatitudeDestiny(),
                ent.getLongitudeDestiny(),
                ent.getDistUnit(),
                ent.getDistScalar()
        );
    }
}
