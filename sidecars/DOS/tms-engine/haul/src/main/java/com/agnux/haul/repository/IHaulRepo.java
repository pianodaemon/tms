package com.agnux.haul.repository;

import com.agnux.haul.errors.TmsException;
import com.agnux.haul.repository.model.CargoAssignment;
import com.agnux.haul.repository.model.Vehicle;
import com.agnux.haul.repository.model.Agreement;
import java.util.UUID;

public interface IHaulRepo {

    String createCargoAssignment(CargoAssignment t);

    Vehicle getAvailableVehicule(UUID vehicleId);

    UUID createVehicle(Vehicle v) throws TmsException;

    void editVehicle(Vehicle v) throws TmsException;

    Agreement getAvailableAgreement(UUID agreementId);
}
