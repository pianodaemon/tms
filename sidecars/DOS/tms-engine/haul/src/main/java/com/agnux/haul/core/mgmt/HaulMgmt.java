package com.agnux.haul.core.mgmt;

import com.agnux.haul.repositories.Route;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import com.agnux.haul.repositories.IHaulRepo;
import java.math.BigDecimal;

@AllArgsConstructor
public class HaulMgmt {

    @NonNull
    private IHaulRepo repo;

    public String assignTrip(
            final @NonNull TripDto tripData,
            final @NonNull String vehicleId,
            final @NonNull String tenantId) {

        Route route = new Route();
        route.setTenantId(tenantId);

        /* aqui se deben de tomar 
           todos los datos de los Dto,
           cargar variables especificas para el tenant,
           validar el uso de el vehiculo pasado como argumento,
           calcular consumo de combustible en base a variables
           (quizas esto ya paso en la UI, pero se tiene que hacer aqui de nuevo)
        */
        route.setFuelConsumption(BigDecimal.ZERO);

        /*
           executar otros calculos necesarios,
           comunicacion con otros programas distribuidos (aqui se obtienen datos de otros microservicios)
           y ejecutar toda la logica de
           negocio necesaria para
           para setear una instancia de el modelo Route */

        return repo.createRoute(route);
    }
}
