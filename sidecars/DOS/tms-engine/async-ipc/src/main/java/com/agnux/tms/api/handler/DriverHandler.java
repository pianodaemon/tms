package com.agnux.tms.api.handler;

import com.agnux.tms.api.dto.PatioDto;
import com.agnux.tms.api.service.PatioService;
import com.agnux.tms.repository.model.Patio;
import java.util.UUID;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class PatioHandler extends ScaffoldHandler<Patio, PatioDto> {

    public PatioHandler(PatioService service) {
        super(service, PatioHandler::entMapper, PatioHandler::dtoMapper, PatioDto.class);
    }

    private static Patio entMapper(PatioDto dto, UUID tenantId) {
        UUID id = dto.getId();
        String name = dto.getName();
        return new Patio(id, tenantId, name, dto.getLatitudeLocation(), dto.getLongitudeLocation());
    }

    private static PatioDto dtoMapper(Patio ent) {
        UUID id = ent.getId().orElseThrow();
        String name = ent.getName();
        return new PatioDto(id, name, ent.getLatitudeLocation(), ent.getLongitudeLocation());
    }
}
