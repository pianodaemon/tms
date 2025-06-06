package com.agnux.tms.api.service;

import com.agnux.tms.errors.TmsException;
import com.agnux.tms.repository.IHaulRepo;
import com.agnux.tms.repository.PaginationSegment;
import com.agnux.tms.repository.model.Driver;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class DriverService implements CrudService<Driver> {

    private final IHaulRepo repo;

    @Override
    public UUID create(Driver entity) throws TmsException {
        UUID id = repo.createDriver(entity);
        log.info("Created driver with UUID: " + id);
        return id;
    }

    @Override
    public Driver read(UUID id) throws TmsException {
        return repo.getDriver(id);
    }

    @Override
    public void update(Driver entity) throws TmsException {
        UUID id = repo.editDriver(entity);
        log.info("Updated driver with UUID: " + id);
    }

    @Override
    public void delete(UUID id) throws TmsException {
        repo.deleteDriver(id);
        log.info("Deleted driver with UUID: {}", id);
    }

    @Override
    public PaginationSegment<Driver> listPage(UUID tenantId, Map<String, String> filters, Map<String, String> pageParams) throws TmsException {
        log.info("Fetching a driver's page");
        return repo.listDriverPage(filters, pageParams);
    }
}
