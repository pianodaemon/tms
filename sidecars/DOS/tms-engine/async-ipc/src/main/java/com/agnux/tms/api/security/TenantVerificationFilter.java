package com.agnux.tms.api.security;

import com.agnux.tms.errors.TmsException;
import com.agnux.tms.errors.ErrorCodes;
import static com.agnux.tms.api.handler.ServiceResponseHelper.*;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class TenantVerificationFilter implements HandlerFilterFunction<ServerResponse, ServerResponse> {

    private static final String TENANT_CLAIM = "tenantId";
    private static final String FILTER_CONTEXT = "Tenant verification filter";

    @Override
    public Mono<ServerResponse> filter(ServerRequest request, HandlerFunction<ServerResponse> next) {
        String pathTenantId;
        try {
            pathTenantId = request.pathVariable("tenantId");
        } catch (IllegalArgumentException ex) {
            return badRequest(FILTER_CONTEXT, new TmsException("missing tenantId path variable", ex));
        }

        return ReactiveSecurityContextHolder.getContext()
                .map(securityContext -> (Jwt) securityContext.getAuthentication().getPrincipal())
                .flatMap(jwt -> {
                    String tokenTenantId = jwt.getClaimAsString(TENANT_CLAIM);

                    if (!pathTenantId.equals(tokenTenantId)) {
                        log.info("Preventing tenant identifier spoofing");
                        return forbidden(FILTER_CONTEXT, new TmsException("Tenant identifier mismatch", ErrorCodes.LACK_OF_DATA_INTEGRITY));
                    }

                    return next.handle(request);
                })
                .switchIfEmpty(unauthorized(FILTER_CONTEXT, new TmsException("Unauthorized to access this resource", ErrorCodes.LACK_OF_PERMISSIONS)));
    }
}
