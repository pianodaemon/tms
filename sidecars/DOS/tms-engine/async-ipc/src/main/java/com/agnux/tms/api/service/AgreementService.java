package com.agnux.tms.api.service;

import com.agnux.tms.errors.TmsException;
import com.agnux.tms.repository.IHaulRepo;
import com.agnux.tms.repository.PaginationSegment;
import com.agnux.tms.repository.model.Agreement;

import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class AgreementService implements GenCrudService<Agreement> {

    private final IHaulRepo repo;

    @Override
    public UUID create(Agreement entity) throws TmsException {
        UUID id = repo.createAgreement(entity);
        log.info("Created agreement with UUID: {}", id);
        return id;
    }

    @Override
    public Agreement read(UUID id) throws TmsException {
        return repo.getAgreement(id);
    }

    @Override
    public void update(Agreement entity) throws TmsException {
        UUID id = repo.editAgreement(entity);
        log.info("Updated agreement with UUID: {}", id);
    }

    @Override
    public void delete(UUID id) throws TmsException {
        repo.deleteAgreement(id);
        log.info("Deleted agreement with UUID: {}", id);
    }

    @Override
    public PaginationSegment<Agreement> listPage(UUID tenantId, Map<String, String> filters, Map<String, String> pageParams) throws TmsException {
        log.info("Fetching a agreement's page");
        return repo.listAgreementPage(filters, pageParams);
    }
}
