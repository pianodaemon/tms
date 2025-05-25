package com.agnux.tms.api.handler;

import com.agnux.tms.api.service.AgreementService;
import com.agnux.tms.repository.model.Agreement;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class AgreementHandler extends GenCrudHandler<Agreement> {

    public AgreementHandler(AgreementService service) {
        super(service);
    }
}
