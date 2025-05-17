package com.agnux.tms.api.handler;

import java.util.UUID;

import com.agnux.tms.repository.model.Patio;
import com.agnux.tms.errors.TmsException;
import com.agnux.tms.repository.IHaulRepo;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

@Component
@RequiredArgsConstructor
@Log4j2
public class PatioHandler extends GenCrudHandler<Patio> {

    private final IHaulRepo repo;

    @Override
    protected UUID createEntity(Patio entity) throws TmsException {
        UUID id = repo.createPatio(entity);
        log.info("created patio with UUID: " + id);
        return id;
    }

    @Override
    protected Patio getEntity(UUID id) throws TmsException {
        return repo.getPatio(id);
    }

    @Override
    protected UUID updateEntity(Patio entity) throws TmsException {
        UUID id = repo.editPatio(entity);
        log.info("updated patio with UUID: " + id);
        return id;
    }

    @Override
    protected void deleteEntity(UUID id) throws TmsException {
        repo.deletePatio(id);
    }
}
