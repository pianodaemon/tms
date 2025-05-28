package com.agnux.tms.api.handler;

import com.agnux.tms.api.dto.PatioDto;
import com.agnux.tms.api.service.PatioService;
import com.agnux.tms.errors.TmsException;
import com.agnux.tms.repository.model.Patio;
import java.util.UUID;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class PatioHandler extends ScaffoldHandler<Patio, PatioDto> {

    public PatioHandler(PatioService service) {
        super(service);
    }

    @Override
    protected Patio entMapper(PatioDto dto, UUID tenantId) throws TmsException {
        UUID id = dto.getId();
        String name = dto.getName();
        return new Patio(id, tenantId, name, dto.getLatitudeLocation(), dto.getLongitudeLocation());
    }

    @Override
    protected PatioDto dtoMapper(Patio ent) {
        UUID id = ent.getId().orElseThrow();
        String name = ent.getName();
        return new PatioDto(id, name, ent.getLatitudeLocation(), ent.getLongitudeLocation());
    }
}
