package com.agnux.haul.repositories;

public interface IHaulRepo {

    String createCargoAssignment(CargoAssignment t);
    
    Vehicle getAvailableVehicule(String vehicleIdRef);
}
