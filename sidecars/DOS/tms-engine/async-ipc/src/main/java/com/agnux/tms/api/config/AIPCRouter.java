package com.agnux.tms.api.config;

import com.agnux.tms.api.handler.*;
import com.agnux.tms.repository.model.TmsBasicModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.function.server.RouterFunctions;

import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.nest;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;

@Configuration
public class AIPCRouter {

    private static final String CATALOGS_API_PATH = "adm";
    private static final String HAUL_API_PATH = "oper";

    private static <T extends TmsBasicModel> RouterFunction<ServerResponse> crudRoutes(final String pathPrefix, GenCrudHandler<T> handler) {
        RouterFunction<ServerResponse> routes = route(GET(pathPrefix + "/{id}"), handler::read)
                .andRoute(POST(pathPrefix), handler::create)
                .andRoute(PUT(pathPrefix), handler::update)
                .andRoute(DELETE(pathPrefix + "/{id}"), handler::delete)
                .andRoute(GET(pathPrefix), handler::listPaginated);

        return routes;
    }

    @Bean
    public RouterFunction<ServerResponse> admRouter(
            AgreementHandler agreementHandler,
            VehicleHandler vehicleHandler,
            CustomerHandler customerHandler,
            DriverHandler driverHandler,
            PatioHandler patioHandler) {

        return nest(path("/" + CATALOGS_API_PATH),
                crudRoutes("/vehicles", vehicleHandler)
                        .and(crudRoutes("/agreements", agreementHandler))
                        .and(crudRoutes("/customers", customerHandler))
                        .and(crudRoutes("/drivers", driverHandler))
                        .and(crudRoutes("/patios", patioHandler)));
    }

    public static RouterFunction<ServerResponse> haulMgmtRoutes(HaulMgmtHandler handler) {
        return RouterFunctions
                .route(POST("/{tenantId}/{userId}/assign-trip"), handler::assignTrip)
                .andRoute(GET("/{tenantId}/{userId}/estimate-fuel"), handler::estimateFuel);
    }

    @Bean
    public RouterFunction<ServerResponse> haulRouter(HaulMgmtHandler haulHandler) {

        return nest(path("/" + HAUL_API_PATH),
                haulMgmtRoutes(haulHandler)
        );
    }

}
