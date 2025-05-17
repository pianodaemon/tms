package com.agnux.tms.api.config;

import com.agnux.tms.api.handler.DriverHandler;
import com.agnux.tms.api.handler.HaulMgmtHandler;
import com.agnux.tms.api.handler.PatioHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import org.springframework.web.reactive.function.server.RouterFunctions;
import static org.springframework.web.reactive.function.server.RouterFunctions.nest;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class AIPCRouter {

    private static final String CATALOGS_API_PATH = "adm";
    private static final String HAUL_API_PATH = "operation";

    public static RouterFunction<ServerResponse> patioRoutes(PatioHandler handler) {
        return route(GET("/patios/{id}"), handler::readPatio)
                .andRoute(POST("/patios"), handler::createPatio)
                .andRoute(PUT("/patios"), handler::updatePatio)
                .andRoute(DELETE("/patios/{id}"), handler::deletePatio);
    }

    public static RouterFunction<ServerResponse> driverRoutes(DriverHandler handler) {
        return route(GET("/drivers/{id}"), handler::readDriver)
                .andRoute(POST("/drivers"), handler::createDriver)
                .andRoute(PUT("/drivers"), handler::updateDriver)
                .andRoute(DELETE("/drivers/{id}"), handler::deleteDriver);
    }

    @Bean
    public RouterFunction<ServerResponse> admRouter(DriverHandler driverHandler, PatioHandler patioHandler) {
        return nest(path("/" + CATALOGS_API_PATH),
                driverRoutes(driverHandler)
                        .and(patioRoutes(patioHandler))
        );
    }

    public static RouterFunction<ServerResponse> routeHaulMgmt(HaulMgmtHandler handler) {
        return RouterFunctions
                .route(POST("/{tenantId}/{userId}/assign-trip"), handler::assignTrip)
                .andRoute(GET("/{tenantId}/{userId}/estimate-fuel"), handler::estimateFuel);
    }

    @Bean
    public RouterFunction<ServerResponse> haulRouter(
            HaulMgmtHandler haulHandler) {

        return nest(path("/" + HAUL_API_PATH),
                routeHaulMgmt(haulHandler)
        );
    }

}
