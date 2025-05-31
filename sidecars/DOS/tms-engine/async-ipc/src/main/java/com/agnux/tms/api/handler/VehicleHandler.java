package com.agnux.tms.api.handler;

import com.agnux.tms.api.dto.VehicleDto;
import com.agnux.tms.api.service.VehicleService;
import com.agnux.tms.errors.TmsException;
import com.agnux.tms.repository.model.Vehicle;

import java.util.UUID;

import org.springframework.stereotype.Component;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class VehicleHandler extends ScaffoldHandler<Vehicle, VehicleDto> {

    public VehicleHandler(VehicleService service) {
        super(service);
    }

    @Override
    protected Vehicle entMapper(VehicleDto dto, UUID tenantId) throws TmsException {
        return new Vehicle(dto.getId(), tenantId,
                dto.getNumberPlate(), dto.getNumberPlateExpiration(), dto.getNumberSerial(),
                dto.getVehicleType(),
                dto.getVehicleColor(),
                dto.getVehicleYear(), dto.getFederalConf(),
                dto.getPerfDistUnit(), dto.getPerfVolUnit(), dto.getPerfScalar());
    }

    @Override
    protected VehicleDto dtoMapper(Vehicle ent) {
        UUID id = ent.getId().orElseThrow();
        return new VehicleDto(
                id,
                ent.getNumberPlate(),
                ent.getNumberPlateExpiration(),
                ent.getNumberSerial(),
                ent.getVehicleType(),
                ent.getVehicleColor(),
                ent.getVehicleYear(),
                ent.getFederalConf(),
                ent.getPerfDistUnit(),
                ent.getPerfVolUnit(),
                ent.getPerfScalar()
        );
    }
}
