package com.agnux.haul.repository;

import com.agnux.haul.repository.model.CargoAssignment;
import com.agnux.haul.repository.model.Vehicle;
import com.agnux.haul.repository.model.Agreement;

public interface IHaulRepo {

    String createCargoAssignment(CargoAssignment t);
    
    Vehicle getAvailableVehicule(String vehicleIdRef);
    
    Agreement getAvailableAgreement(String agreementRef);
}
