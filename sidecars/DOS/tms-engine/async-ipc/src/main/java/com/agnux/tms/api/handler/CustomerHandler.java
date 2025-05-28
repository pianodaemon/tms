package com.agnux.tms.api.handler;

import com.agnux.tms.api.dto.CustomerDto;
import com.agnux.tms.api.service.CustomerService;
import com.agnux.tms.errors.TmsException;
import com.agnux.tms.repository.model.Customer;
import java.util.UUID;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class CustomerHandler extends ScaffoldHandler<Customer, CustomerDto> {

    public CustomerHandler(CustomerService service) {
        super(service);
    }

    @Override
    protected Customer entMapper(CustomerDto dto, UUID tenantId) throws TmsException {
        UUID id = dto.getId();
        String name = dto.getName();
        var ent = new Customer(id, tenantId, name);
        return ent;
    }

    @Override
    protected CustomerDto dtoMapper(Customer ent) {
        UUID id = ent.getId().orElseThrow();
        String name = ent.getName();
        CustomerDto dto = new CustomerDto();
        dto.setId(id);
        dto.setName(name);
        return dto;
    }
}
