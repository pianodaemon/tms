package com.agnux.tms.api.handler;

import com.agnux.tms.api.service.CustomerService;
import com.agnux.tms.repository.model.Customer;
import org.springframework.stereotype.Component;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class CustomerHandler extends GenCrudHandler<Customer> {

    public CustomerHandler(CustomerService service) {
        super(service);
    }
}
