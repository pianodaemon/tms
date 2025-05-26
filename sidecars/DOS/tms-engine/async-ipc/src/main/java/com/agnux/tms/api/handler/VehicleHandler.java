package com.agnux.tms.api.handler;

import com.agnux.tms.api.service.VehicleService;
import com.agnux.tms.repository.model.Vehicle;

import org.springframework.stereotype.Component;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class VehicleHandler extends CrudHandler<Vehicle> {

    public VehicleHandler(VehicleService service) {
        super(service);
    }
}
