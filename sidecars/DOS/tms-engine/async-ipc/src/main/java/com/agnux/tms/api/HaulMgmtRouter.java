package com.agnux.tms.api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.*;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class HaulMgmtRouter {

    @Bean
    public RouterFunction<ServerResponse> routeHaulMgmt(HaulMgmtHandler handler) {
        return RouterFunctions
                .route(POST("/haul/{tenantId}/{userId}/assign-trip"), handler::assignTrip)
                .andRoute(GET("/haul/{tenantId}/{userId}/estimate-fuel"), handler::estimateFuel);
    }
}
