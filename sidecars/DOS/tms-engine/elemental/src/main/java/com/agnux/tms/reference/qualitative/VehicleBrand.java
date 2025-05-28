package com.agnux.tms.reference.qualitative;

public enum VehicleBrand {

    TOYOTA,
    FORD,
    CHEVROLET,
    VOLKSWAGEN,
    HONDA,
    NISSAN,
    MERCEDES_BENZ,
    BMW,
    AUDI,
    KIA,
    HYUNDAI,
    MAZDA,
    JEEP,
    DODGE,
    RAM,
    GMC,
    TESLA,
    VOLVO,
    SCANIA,
    MAN,
    INTERNATIONAL,
    MACK,
    FREIGHTLINER,
    KENWORTH,
    PETERBILT,
    ISUZU,
    HINO;

    public String getDescription() {
        return name().replace("_", " ");
    }
}
