package com.agnux.tms.api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class PatioRouter {

    @Bean
    public RouterFunction<ServerResponse> patioRoutes(PatioHandler handler) {
        return route(GET("/patios/{id}"), handler::readPatio)
                .andRoute(POST("/patios"), handler::createPatio)
                .andRoute(PUT("/patios"), handler::updatePatio)
                .andRoute(DELETE("/patios/{id}"), handler::deletePatio);
    }
}