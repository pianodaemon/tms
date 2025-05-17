package com.agnux.tms.api.handler;

import java.util.UUID;

import com.agnux.tms.repository.model.Driver;
import com.agnux.tms.errors.TmsException;
import com.agnux.tms.repository.IHaulRepo;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

@Component
@RequiredArgsConstructor
@Log4j2
public class DriverHandler extends GenCrudHandler<Driver> {

    private final IHaulRepo repo;

    @Override
    protected UUID createEntity(Driver entity) throws TmsException {
        UUID id = repo.createDriver(entity);
        log.info("created driver with UUID: " + id);
        return id;
    }

    @Override
    protected Driver getEntity(UUID id) throws TmsException {
        return repo.getDriver(id);
    }

    @Override
    protected UUID updateEntity(Driver entity) throws TmsException {
        UUID id = repo.editDriver(entity);
        log.info("updated driver with UUID: " + id);
        return id;
    }

    @Override
    protected void deleteEntity(UUID id) throws TmsException {
        repo.deleteDriver(id);
    }
}
