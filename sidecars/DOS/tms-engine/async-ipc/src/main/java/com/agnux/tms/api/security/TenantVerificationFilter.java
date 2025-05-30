package com.agnux.tms.api.security;

import com.agnux.tms.errors.TmsException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import static com.agnux.tms.api.handler.ServiceResponseHelper.*;

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

        return ReactiveSecurityContextHolder.getContext()
                .map(securityContext -> (Jwt) securityContext.getAuthentication().getPrincipal())
                .flatMap(jwt -> {
                    String tokenTenantId = jwt.getClaimAsString(TENANT_CLAIM);

                    if (!pathTenantId.equals(tokenTenantId)) {
                        return ServerResponse.status(403).bodyValue("Tenant ID mismatch");
                    }

                    return next.handle(request);
                })
                .switchIfEmpty(ServerResponse.status(401).bodyValue("Unauthorized"));
    }
}
