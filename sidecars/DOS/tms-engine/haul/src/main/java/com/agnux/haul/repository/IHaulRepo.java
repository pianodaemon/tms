package com.agnux.haul.repository;

import com.agnux.haul.repository.model.CargoAssignment;
import com.agnux.haul.repository.model.Vehicle;
import com.agnux.haul.repository.model.Agreement;
import java.util.UUID;

public interface IHaulRepo {

    String createCargoAssignment(CargoAssignment t);
    
    Vehicle getAvailableVehicule(UUID vehicleId);
    
    Vehicle createVehicle();

    Agreement getAvailableAgreement(UUID agreementId);
}
