package com.agnux.tms.core.mgmt;

import java.math.BigDecimal;

import com.agnux.tms.errors.ErrorCodes;
import com.agnux.tms.errors.TmsException;
import com.agnux.tms.repository.model.Agreement;
import com.agnux.tms.repository.model.CargoAssignment;
import com.agnux.tms.repository.IHaulRepo;
import com.agnux.tms.repository.model.TransLogRecord;
import com.agnux.tms.repository.model.Vehicle;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

@Log4j2
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
        Vehicle ship = repo.getVehicule(tripDetails.getVehicleId());

        if (!ship.getTenantId().equals(tenantDetails.getTenantId())) {
            final String emsg = "The assigned vehicle does not pertain to tenant " + tenantDetails.getTenantId();
            throw new TmsException(emsg, ErrorCodes.LACKOF_DATA_INTEGRITY);
        }

        // Si el convenio no esta disponible 
        // Una exception sera levantada con el respectivo codigo de error
        // para esta situacion
        Agreement agreement = repo.getAgreement(tripDetails.getAgreementId());

        if (!agreement.getTenantId().equals(tenantDetails.getTenantId())) {
            final String emsg = "The assigned agreement does not pertain to tenant " + tenantDetails.getTenantId();
            throw new TmsException(emsg, ErrorCodes.LACKOF_DATA_INTEGRITY);
        }

        // calcular consumo de combustible en base a variables
        // (quizas esto ya paso en la UI, pero se tiene que hacer aqui de nuevo)
        // Si esto require a una recalibracion 
        // Una exception sera levantada con el respectivo codigo de error
        BigDecimal fuelEstimated = estimateFuel(tenantDetails, tripDetails.getVehicleId(), tripDetails.getAgreementId());

        // A fake on for now
        CargoAssignment cas = new CargoAssignment(null, tenantDetails.getTenantId(), tripDetails.getDriverId(), ship.getId().get(), 20.0, -99.3);

        // Salva la asignacion sobre la base de datos elegida
        // para este microservicio
        // y retornara una referencia/ID que nos permitira localizar
        // el cargo para otros casos de uso que asi lo requieran
        UUID cargoId = repo.createCargoAssignment(cas);

        TransLogRecord tlRecord = new TransLogRecord(null, tenantDetails.getTenantId(), agreement.getDistUnit(), cargoId, agreement.getDistScalar(), fuelEstimated);
        UUID createTransLogRecordId = repo.createTransLogRecord(tlRecord);

        log.info("Created cargo assigment ID " + cargoId.toString() + " along with transport log record ID " + createTransLogRecordId.toString());
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
            final @NonNull UUID vehicleId,
            final @NonNull UUID agreementId) throws TmsException {

        // Aqui se tiene que cargar y ejecutar el algoritmo de estimacion 
        // de combustible de el respectivo tenant
        return BigDecimal.ZERO;
    }
}
