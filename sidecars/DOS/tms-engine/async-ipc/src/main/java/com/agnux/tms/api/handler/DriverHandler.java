package com.agnux.tms.api.handler;

import com.agnux.tms.api.dto.DriverDto;
import com.agnux.tms.api.service.DriverService;
import com.agnux.tms.errors.TmsException;
import com.agnux.tms.repository.model.Driver;
import java.util.UUID;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class DriverHandler extends ScaffoldHandler<Driver, DriverDto> {

    public DriverHandler(DriverService service) {
        super(service);
    }

    @Override
    protected Driver entMapper(DriverDto dto, UUID tenantId) throws TmsException {
        UUID id = dto.getId();
        String name = dto.getName();
        return new Driver(id, tenantId, name, dto.getFirstSurname(), dto.getSecondSurname(), dto.getLicenseNumber());
    }

    @Override
    protected DriverDto dtoMapper(Driver ent) {
        UUID id = ent.getId().orElseThrow();
        String name = ent.getName();
        return new DriverDto(id, name, ent.getFirstSurname(), ent.getSecondSurname(), ent.getLicenseNumber());
    }
}
