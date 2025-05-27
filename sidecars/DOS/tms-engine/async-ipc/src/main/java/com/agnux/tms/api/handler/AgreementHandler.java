package com.agnux.tms.api.handler;

import com.agnux.tms.api.dto.AgreementDto;
import com.agnux.tms.api.service.AgreementService;
import com.agnux.tms.repository.model.Agreement;
import java.util.UUID;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class AgreementHandler extends ScaffoldHandler<Agreement, AgreementDto> {

    public AgreementHandler(AgreementService service) {
        super(service, AgreementHandler::entMapper, AgreementHandler::dtoMapper, AgreementDto.class);
    }

    private static Agreement entMapper(AgreementDto dto, UUID u) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private static AgreementDto dtoMapper(Agreement ent) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
