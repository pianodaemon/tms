package com.agnux.haul.repository.model;

public enum VehicleType {

    DRY_VAN("Dry Van"), // Caja seca 
    REFRIGERATED_VAN("Refrigerated Van"), // Caja refrigerada
    FLATBED_TRAILER("Flatbed Trailer"), // Plataforma
    TANKER_TRUCK("Tanker Truck"), // Pipa
    TANDEM_TRUCK("Tandem Truck"), // Torton
    PICKUP_TRUCK("Pickup Truck"), // Camioneta
    DELIVERY_TRUCK("Delivery Truck"),
    MOTORCYCLE("Motorcycle"), // Moto
    CAR("Car"); // Carro

    private final String description;

    VehicleType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
