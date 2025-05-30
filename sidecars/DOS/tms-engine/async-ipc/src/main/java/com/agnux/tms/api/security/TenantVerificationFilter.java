package com.agnux.tms.api.security;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

@Component
public class TenantVerificationFilter implements HandlerFilterFunction<ServerResponse, ServerResponse> {

    private static final String TENANT_CLAIM = "tenantId";

    @Override
    public Mono<ServerResponse> filter(ServerRequest request, HandlerFunction<ServerResponse> next) {
        String pathTenantId = request.pathVariable("tenantId");
        return next.handle(request);
    }
}