package com.agnux.tms.api.service;

import com.agnux.tms.errors.TmsException;
import com.agnux.tms.repository.IHaulRepo;
import com.agnux.tms.repository.PaginationSegment;
import com.agnux.tms.repository.model.Vehicle;
import java.util.UUID;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Component
@RequiredArgsConstructor
@Log4j2
public class VehicleService implements GenCrudService<Vehicle> {

    private final IHaulRepo repo;

    @Override
    public UUID create(Vehicle entity) throws TmsException {
        UUID id = repo.createVehicle(entity);
        log.info("Created vehicle with UUID: " + id);
        return id;
    }

    @Override
    public Vehicle read(UUID id) throws TmsException {
        return repo.getVehicle(id);
    }

    @Override
    public void update(Vehicle entity) throws TmsException {
        UUID id = repo.editVehicle(entity);
        log.info("Updated vehicle with UUID: " + id);
    }

    @Override
    public void delete(UUID id) throws TmsException {
        repo.deleteVehicle(id);
        log.info("Deleted vehicle with UUID: {}", id);
    }

    @Override
    public PaginationSegment<Vehicle> listPage(UUID tenantId, Integer page, Integer size) throws TmsException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
