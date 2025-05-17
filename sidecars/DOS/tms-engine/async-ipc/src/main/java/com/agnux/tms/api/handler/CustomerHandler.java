package com.agnux.tms.api.handler;

import java.util.UUID;

import com.agnux.tms.repository.model.Customer;
import com.agnux.tms.errors.TmsException;
import com.agnux.tms.repository.IHaulRepo;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

@Component
@RequiredArgsConstructor
@Log4j2
public class CustomerHandler extends GenCrudHandler<Customer> {

    private final IHaulRepo repo;

    @Override
    protected UUID createEntity(Customer entity) throws TmsException {
        UUID id = repo.createCustomer(entity);
        log.info("created customer with UUID: " + id);
        return id;
    }

    @Override
    protected Customer getEntity(UUID id) throws TmsException {
        return repo.getCustomer(id);
    }

    @Override
    protected UUID updateEntity(Customer entity) throws TmsException {
        UUID id = repo.editCustomer(entity);
        log.info("updated customer with UUID: " + id);
        return id;
    }

    @Override
    protected void deleteEntity(UUID id) throws TmsException {
        repo.deleteCustomer(id);
    }
}
