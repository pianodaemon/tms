package com.agnux.tms.api.handler;

import com.agnux.tms.api.service.PatioService;
import com.agnux.tms.repository.model.Patio;

import org.springframework.stereotype.Component;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class PatioHandler extends CrudHandler<Patio> {

    public PatioHandler(PatioService service) {
        super(service);
    }
}
