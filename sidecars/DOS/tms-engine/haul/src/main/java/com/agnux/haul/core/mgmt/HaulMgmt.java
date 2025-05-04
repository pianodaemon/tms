package com.agnux.haul.core.mgmt;

import com.agnux.haul.errors.ErrorCodes;
import com.agnux.haul.errors.TmsException;
import com.agnux.haul.repository.model.Agreement;
import com.agnux.haul.repository.model.CargoAssignment;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import com.agnux.haul.repository.IHaulRepo;
import com.agnux.haul.repository.model.TransLogRecord;
import com.agnux.haul.repository.model.Vehicle;
import java.math.BigDecimal;
import java.util.UUID;

@AllArgsConstructor
public class HaulMgmt {

    @NonNull
    private IHaulRepo repo;

    public UUID assignTrip(
            final @NonNull TenantDetailsDto tenantDetails,
            final @NonNull TripDetailsDto tripDetails) throws TmsException {

        /* 
           En este punto se deben cargar criterios especificos para el tenant
           y para el user, de esa manera se hacen efectivos los roles
           tambien conocidos como ACLs (access control lists)
         */
        // Si el vehiculo no esta disponible 
        // Una exception sera levantada con el respectivo codigo de error
        // para esta situacion
        Vehicle ship = repo.getAvailableVehicule(tripDetails.getVehicleId());

        if (!ship.getTenantId().equals(tenantDetails.getTenantId())) {
            final String emsg = "The assigned vehicle does not pertain to tenant " + tenantDetails.getTenantId();
            throw new TmsException(emsg, ErrorCodes.LACKOF_DATA_INTEGRITY);
        }

        // Si el convenio no esta disponible 
        // Una exception sera levantada con el respectivo codigo de error
        // para esta situacion
        Agreement agreement = repo.getAvailableAgreement(tripDetails.getAgreementId());

        if (!agreement.getTenantId().equals(tenantDetails.getTenantId())) {
            final String emsg = "The assigned agreement does not pertain to tenant " + tenantDetails.getTenantId();
            throw new TmsException(emsg, ErrorCodes.LACKOF_DATA_INTEGRITY);
        }

        // calcular consumo de combustible en base a variables
        // (quizas esto ya paso en la UI, pero se tiene que hacer aqui de nuevo)
        // Si esto require a una recalibracion 
        // Una exception sera levantada con el respectivo codigo de error
        BigDecimal fuelEstimated = estimateFuel(tenantDetails, tripDetails);

        // A fake on for now
        UUID driverId =  UUID.fromString("0a232802-d6e8-458f-9eca-6a8c2b980900");
        CargoAssignment cas = new CargoAssignment(null, tenantDetails.getTenantId(), driverId, ship.getId().get());

        // Salva la asignacion sobre la base de datos elegida
        // para este microservicio
        // y retornara una referencia/ID que nos permitira localizar
        // el cargo para otros casos de uso que asi lo requieran
        UUID cargoId = repo.createCargoAssignment(cas);

        // Aqui necesitamos como siguiente linea salvar a database
        TransLogRecord tlRecord = new TransLogRecord(null, tenantDetails.getTenantId(), agreement.getDistUnit(), cargoId, agreement.getDistScalar(), fuelEstimated);

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
            final @NonNull TripDetailsDto tripData) throws TmsException {

        // Aqui se tiene que cargar y ejecutar el algoritmo de estimacion 
        // de combustible de el respectivo tenant
        return BigDecimal.ZERO;
    }
}
