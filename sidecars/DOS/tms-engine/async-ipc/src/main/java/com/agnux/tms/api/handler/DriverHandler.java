package com.agnux.tms.api.handler;

import com.agnux.tms.api.service.DriverService;
import com.agnux.tms.repository.model.Driver;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class DriverHandler extends CrudHandler<Driver> {

    public DriverHandler(DriverService service) {
        super(service);
    }
}
