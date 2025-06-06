package com.agnux.tms.api.handler;

import com.agnux.tms.api.dto.BoxDto;
import com.agnux.tms.api.service.BoxService;
import com.agnux.tms.errors.TmsException;
import com.agnux.tms.repository.model.Box;
import java.util.UUID;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class BoxHandler extends ScaffoldHandler<Box, BoxDto> {

    public BoxHandler(BoxService service) {
        super(service);
    }

    @Override
    protected Box entMapper(BoxDto dto, UUID tenantId) throws TmsException {
        UUID id = dto.getId();
        String name = dto.getName();
        String numberPlate = dto.getNumberPlate();
        return new Box(id, tenantId, name, dto.getBoxType(),
                dto.getBrand(), dto.getNumberOfAxis(), dto.getNumberSerial(), numberPlate,
                dto.getNumberPlateExpiration(), dto.getBoxYear(), dto.isLease());
    }

    @Override
    protected BoxDto dtoMapper(Box ent) {
        UUID id = ent.getId().orElseThrow();
        String name = ent.getName();
        BoxDto dto = new BoxDto();
        dto.setId(id);
        dto.setName(name);
        dto.setBoxType(ent.getBoxType());
        dto.setBrand(ent.getBrand());
        dto.setNumberOfAxis(ent.getNumberOfAxis());
        dto.setNumberSerial(ent.getNumberSerial());
        dto.setNumberPlate(ent.getNumberPlate());
        dto.setNumberPlateExpiration(ent.getNumberPlateExpiration());
        dto.setBoxYear(ent.getBoxYear());
        dto.setLease(ent.isLease());
        return dto;
    }
}
