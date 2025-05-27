package com.agnux.tms.api.handler;

import com.agnux.tms.api.dto.VehicleDto;
import com.agnux.tms.api.service.VehicleService;
import com.agnux.tms.repository.model.Vehicle;

import java.util.UUID;

import org.springframework.stereotype.Component;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class VehicleHandler extends ScaffoldHandler<Vehicle, VehicleDto> {

    public VehicleHandler(VehicleService service) {
        super(service, VehicleHandler::entMapper, VehicleHandler::dtoMapper, VehicleDto.class);
    }

    private static Vehicle entMapper(VehicleDto dto, UUID tenantId) {
        return new Vehicle(dto.getId(), tenantId,
                dto.getNumberPlate(), dto.getNumberSerial(),
                dto.getVehicleType(),
                dto.getVehicleColor(),
                dto.getVehicleYear(), dto.getFederalConf(),
                dto.getPerfDistUnit(), dto.getPerfVolUnit(), dto.getPerfScalar());
    }

    private static VehicleDto dtoMapper(Vehicle ent) {
        UUID id = ent.getId().orElseThrow();
        VehicleDto dto = new VehicleDto(
                id,
                ent.getNumberPlate(),
                ent.getNumberSerial(),
                ent.getVehicleType(),
                ent.getVehicleColor(),
                ent.getVehicleYear(),
                ent.getFederalConf(),
                ent.getPerfDistUnit(),
                ent.getPerfVolUnit(),
                ent.getPerfScalar()
        );

        return dto;
    }
}
