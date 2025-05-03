package com.agnux.haul.repository;

import com.agnux.haul.errors.TmsException;
import com.agnux.haul.repository.model.CargoAssignment;
import com.agnux.haul.repository.model.Vehicle;
import com.agnux.haul.repository.model.Agreement;
import com.agnux.haul.repository.model.Driver;
import com.agnux.haul.repository.model.Patio;
import java.util.UUID;

public interface IHaulRepo {

    Agreement getAvailableAgreement(UUID agreementId);

    String createCargoAssignment(CargoAssignment t);

    Vehicle getAvailableVehicule(UUID vehicleId) throws TmsException;

    UUID createVehicle(Vehicle v) throws TmsException;

    UUID editVehicle(Vehicle v) throws TmsException;

    void deleteVehicle(UUID vehicleId) throws TmsException;

    Driver getAvailableDriver(UUID driverId) throws TmsException;

    UUID createDriver(Driver v) throws TmsException;

    UUID editDriver(Driver v) throws TmsException;

    void deleteDriver(UUID driverId) throws TmsException;

    public Patio getAvailablePatio(UUID patioId) throws TmsException;

    public UUID createPatio(Patio p) throws TmsException;

    public UUID editPatio(Patio p) throws TmsException;

    public void deletePatio(UUID patioId) throws TmsException;

}
