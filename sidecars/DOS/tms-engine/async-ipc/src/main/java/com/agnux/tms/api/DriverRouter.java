package com.agnux.tms.api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class DriverRouter {

    @Bean
    public RouterFunction<?> driverRoutes(DriverHandler handler) {
        return route(GET("/drivers/{id}"), handler::readDriver)
                .andRoute(POST("/drivers"), handler::createDriver)
                .andRoute(PUT("/drivers"), handler::updateDriver)
                .andRoute(DELETE("/drivers/{id}"), handler::deleteDriver);
    }
}
