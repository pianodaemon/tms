package com.agnux.haul.repository;

import com.agnux.haul.errors.TmsException;
import com.agnux.haul.repository.model.CargoAssignment;
import com.agnux.haul.repository.model.Vehicle;
import com.agnux.haul.repository.model.Agreement;
import java.util.UUID;

public interface IHaulRepo {

    Agreement getAvailableAgreement(UUID agreementId);

    String createCargoAssignment(CargoAssignment t);

    Vehicle getAvailableVehicule(UUID vehicleId) throws TmsException;

    UUID createVehicle(Vehicle v) throws TmsException;

    UUID editVehicle(Vehicle v) throws TmsException;

    void deleteVehicle(UUID vehicleId) throws TmsException;
}
