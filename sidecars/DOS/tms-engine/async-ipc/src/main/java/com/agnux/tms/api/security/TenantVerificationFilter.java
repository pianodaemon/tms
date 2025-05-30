package com.agnux.tms.api.security;

import static com.agnux.tms.api.handler.ServiceResponseHelper.*;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

@Component
public class TenantVerificationFilter implements HandlerFilterFunction<ServerResponse, ServerResponse> {

    private static final String TENANT_CLAIM = "tenantId";

    @Override
    public Mono<ServerResponse> filter(ServerRequest request, HandlerFunction<ServerResponse> next) {
        String pathTenantId;
        try {
            pathTenantId = request.pathVariable("tenantId");
        } catch (IllegalArgumentException ex) {
            return badRequest("Tenant verification invalid",
                    new TmsException("missing tenantId path variable", ex));
        }
        return next.handle(request);
    }
}
