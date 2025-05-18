package com.agnux.tms.api.service;

import com.agnux.tms.errors.TmsException;
import com.agnux.tms.repository.IHaulRepo;
import com.agnux.tms.repository.model.Customer;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class CustomerService implements GenCrudService<Customer> {

    private final IHaulRepo repo;

    @Override
    public UUID create(Customer entity) throws TmsException {
        UUID id = repo.createCustomer(entity);
        log.info("Created customer with UUID: {}", id);
        return id;
    }

    @Override
    public Customer read(UUID id) throws TmsException {
        return repo.getCustomer(id);
    }

    @Override
    public UUID update(Customer entity) throws TmsException {
        UUID id = repo.editCustomer(entity);
        log.info("Updated customer with UUID: {}", id);
        return id;
    }

    @Override
    public void delete(UUID id) throws TmsException {
        repo.deleteCustomer(id);
        log.info("Deleted customer with UUID: {}", id);
    }
}
