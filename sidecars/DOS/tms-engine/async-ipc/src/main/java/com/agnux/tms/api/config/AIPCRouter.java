package com.agnux.tms.api.config;

import com.agnux.tms.api.handler.*;
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

    public static RouterFunction<ServerResponse> customerRoutes(CustomerHandler handler) {
        return route(GET("/customers/{id}"), handler::read)
                .andRoute(POST("/customers"), handler::create)
                .andRoute(PUT("/customers"), handler::update)
                .andRoute(DELETE("/customers/{id}"), handler::delete);
    }

    public static RouterFunction<ServerResponse> patioRoutes(PatioHandler handler) {
        return route(GET("/patios/{id}"), handler::read)
                .andRoute(POST("/patios"), handler::create)
                .andRoute(PUT("/patios"), handler::update)
                .andRoute(DELETE("/patios/{id}"), handler::delete);
    }

    public static RouterFunction<ServerResponse> driverRoutes(DriverHandler handler) {
        return route(GET("/drivers/{id}"), handler::read)
                .andRoute(POST("/drivers"), handler::create)
                .andRoute(PUT("/drivers"), handler::update)
                .andRoute(DELETE("/drivers/{id}"), handler::delete);
    }

    public static RouterFunction<ServerResponse> haulMgmtRoutes(HaulMgmtHandler handler) {
        return RouterFunctions
                .route(POST("/{tenantId}/{userId}/assign-trip"), handler::assignTrip)
                .andRoute(GET("/{tenantId}/{userId}/estimate-fuel"), handler::estimateFuel);
    }

    @Bean
    public RouterFunction<ServerResponse> admRouter(
            CustomerHandler customerHandler,
            DriverHandler driverHandler,
            PatioHandler patioHandler) {

        return nest(path("/" + CATALOGS_API_PATH),
                driverRoutes(driverHandler)
                        .and(patioRoutes(patioHandler))
                        .and(customerRoutes(customerHandler)));
    }

    @Bean
    public RouterFunction<ServerResponse> haulRouter(HaulMgmtHandler haulHandler) {

        return nest(path("/" + HAUL_API_PATH),
                haulMgmtRoutes(haulHandler)
        );
    }

}
