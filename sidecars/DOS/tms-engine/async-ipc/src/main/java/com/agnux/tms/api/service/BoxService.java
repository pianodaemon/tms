package com.agnux.tms.api.service;

import com.agnux.tms.errors.TmsException;
import com.agnux.tms.repository.IHaulRepo;
import com.agnux.tms.repository.PaginationSegment;
import com.agnux.tms.repository.model.Box;

import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class BoxService implements CrudService<Box> {

    private final IHaulRepo repo;

    @Override
    public UUID create(Box entity) throws TmsException {
        UUID id = repo.createBox(entity);
        log.info("Created box with UUID: {}", id);
        return id;
    }

    @Override
    public Box read(UUID id) throws TmsException {
        return repo.getBox(id);
    }

    @Override
    public void update(Box entity) throws TmsException {
        UUID id = repo.editBox(entity);
        log.info("Updated b with UUID: {}", id);
    }

    @Override
    public void delete(UUID id) throws TmsException {
        repo.deleteBox(id);
        log.info("Deleted box with UUID: {}", id);
    }

    @Override
    public PaginationSegment<Box> listPage(UUID tenantId, Map<String, String> filters, Map<String, String> pageParams) throws TmsException {
        log.info("Fetching a box's page");
        return repo.listBoxPage(filters, pageParams);
    }
}
