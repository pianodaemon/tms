package com.agnux.tms.api.service;

import com.agnux.tms.errors.TmsException;
import com.agnux.tms.repository.IHaulRepo;
import com.agnux.tms.repository.PaginationSegment;
import com.agnux.tms.repository.model.Patio;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Component
@RequiredArgsConstructor
@Log4j2
public class PatioService implements CrudService<Patio> {

    private final IHaulRepo repo;

    @Override
    public UUID create(Patio entity) throws TmsException {
        UUID id = repo.createPatio(entity);
        log.info("Created patio with UUID: " + id);
        return id;
    }

    @Override
    public Patio read(UUID id) throws TmsException {
        return repo.getPatio(id);
    }

    @Override
    public void update(Patio entity) throws TmsException {
        UUID id = repo.editPatio(entity);
        log.info("Updated patio with UUID: " + id);
    }

    @Override
    public void delete(UUID id) throws TmsException {
        repo.deletePatio(id);
        log.info("Deleted patio with UUID: {}", id);
    }

    @Override
    public PaginationSegment<Patio> listPage(UUID tenantId, Map<String, String> filters, Map<String, String> pageParams)  throws TmsException {
        log.info("Fetching a patio's page");
        return repo.listPatioPage(filters, pageParams);
    }
}
