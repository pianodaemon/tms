package com.agnux.haul.core.mgmt;

import com.agnux.haul.errors.ErrorCodes;
import com.agnux.haul.errors.TmsException;
import com.agnux.haul.repositories.CargoAssignment;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import com.agnux.haul.repositories.IHaulRepo;
import com.agnux.haul.repositories.TransLogRecord;
import com.agnux.haul.repositories.Vehicle;
import java.math.BigDecimal;

@AllArgsConstructor
public class HaulMgmt {

    @NonNull
    private IHaulRepo repo;

    public String assignTrip(
            final @NonNull TenantDetailsDto tenantDetails,
            final @NonNull String vehicleId,
            final @NonNull TripDetailsDto tripDetails) throws TmsException {

        /* 
           En este punto se deben cargar criterios especificos para el tenant
           y para el user, de esa manera se hacen efectivos los roles
           tambien conocidos como ACLs (access control lists)
        */
        
        // Si el vehiculo no esta disponible 
        // Una exception sera levantada con el respectivo codigo de error
        // para esta situacion
        Vehicle ship = repo.getAvailableVehicule(vehicleId);

        if (!ship.getTenantId().equals(tenantDetails.getTenantId())) {
            final String emsg = "The assigned vehicle does not pertain to tenant " + tenantDetails.getTenantId();
            throw new TmsException(emsg, ErrorCodes.LACKOF_DATA_INTEGRITY);
        }
 
        // calcular consumo de combustible en base a variables
        // (quizas esto ya paso en la UI, pero se tiene que hacer aqui de nuevo)
        // Si esto require a una recalibracion 
        // Una exception sera levantada con el respectivo codigo de error
        BigDecimal fuelEstimated = estimateFuel(tenantDetails, vehicleId, tripDetails);

        TransLogRecord tlRecord = new TransLogRecord(tripDetails.getDistUnit(), tripDetails.getDistScalar(), fuelEstimated);

        CargoAssignment cas = new CargoAssignment(tenantDetails.getTenantId(), ship, tlRecord);

        // Salva la asignacion sobre la base de datos elegida
        // para este microservicio
        // y retornara una referencia/ID que nos permitira localizar
        // el cargo para otros casos de uso que asi lo requieran
        String cargoId = repo.createCargoAssignment(cas);
        
        // En este punto se puede establecer comunicacion con otros programas distribuidos
        // Que requieran ejecutar acciones relacionadas a la nueva asignacion de cargo

        return cargoId;
    }

    /*
    Determines the quantity of the required fuel
    based on vehicule features and distance   
     */
    public BigDecimal estimateFuel(
            final @NonNull TenantDetailsDto tenantDetails,
            final @NonNull String vehicleId,
            final @NonNull TripDetailsDto tripData) throws TmsException {

        // Aqui se tiene que cargar y ejecutar el algoritmo de estimacion 
        // de combustible de el respectivo tenant
        return BigDecimal.ZERO;
    }
}
