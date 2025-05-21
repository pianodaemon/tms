package com.agnux.tms.api.service;

import com.agnux.tms.errors.TmsException;
import com.agnux.tms.repository.IHaulRepo;
import com.agnux.tms.repository.PaginationSegment;
import com.agnux.tms.repository.model.Customer;
import java.util.HashMap;
import java.util.Map;
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
    public void update(Customer entity) throws TmsException {
        UUID id = repo.editCustomer(entity);
        log.info("Updated customer with UUID: {}", id);
    }

    @Override
    public void delete(UUID id) throws TmsException {
        repo.deleteCustomer(id);
        log.info("Deleted customer with UUID: {}", id);
    }

    @Override
    public PaginationSegment<Customer> listPage(UUID tenantId, Integer page, Integer pageSize) throws TmsException {
        Map<String, String> filters = new HashMap<>();
        filters.put("tenant_id", tenantId.toString());

        Map<String, String> pagination = new HashMap<>();
        pagination.put("per_page", pageSize.toString());
        pagination.put("page", page.toString());
        pagination.put("order_by", "name");

        return repo.listCustomerPage(filters, pagination);
    }
}
