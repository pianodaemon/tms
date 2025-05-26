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
public class RouterConfig {

    private static final String ADM_API_PATH = "adm";
    private static final String HAUL_API_PATH = "oper";

    /*
    private static <T extends TmsBasicModel> RouterFunction<ServerResponse> crudRoutes(final String pathPrefix, ScaffoldHandler<T> handler) {
        RouterFunction<ServerResponse> routes = route(GET(pathPrefix + "/{id}"), handler::read)
                .andRoute(POST(pathPrefix), handler::create)
                .andRoute(PUT(pathPrefix), handler::update)
                .andRoute(DELETE(pathPrefix + "/{id}"), handler::delete)
                .andRoute(GET(pathPrefix), handler::listPaginated);

        return routes;
    }*/
    private static <T extends TmsBasicModel, D> RouterFunction<ServerResponse> crudRoutes(final String pathPrefix, ScaffoldHandler<T, D> handler) {
        RouterFunction<ServerResponse> routes = route(GET(pathPrefix + "/{id}"), handler::read)
                .andRoute(POST(pathPrefix), handler::create)
                .andRoute(PUT(pathPrefix), handler::update)
                .andRoute(DELETE(pathPrefix + "/{id}"), handler::delete)
                .andRoute(GET(pathPrefix), handler::listPaginated);

        return routes;
    }

    private static <T extends TmsBasicModel, D> RouterFunction<ServerResponse> mtCrudRoutes(final String pathPrefix, ScaffoldHandler<T, D> handler) {
        return crudRoutes(pathPrefix + "/{tenantId}", handler);
    }

    @Bean
    public RouterFunction<ServerResponse> admRouter(CustomerHandler customerHandler) {

        return nest(path("/" + ADM_API_PATH),
                mtCrudRoutes("/customers", customerHandler));
    }

    /*
    @Bean
    public RouterFunction<ServerResponse> admRouter(
            AgreementHandler agreementHandler,
            VehicleHandler vehicleHandler,
            CustomerHandler customerHandler,
            DriverHandler driverHandler,
            PatioHandler patioHandler) {

        return nest(path("/" + ADM_API_PATH),
                mtCrudRoutes("/vehicles", vehicleHandler)
                        .and(mtCrudRoutes("/agreements", agreementHandler))
                        .and(mtCrudRoutes("/customers", customerHandler))
                        .and(mtCrudRoutes("/drivers", driverHandler))
                        .and(mtCrudRoutes("/patios", patioHandler)));
    }
     */
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
