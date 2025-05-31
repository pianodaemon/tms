package com.agnux.tms.api.security;

import static com.agnux.tms.api.handler.ServiceResponseHelper.forbidden;
import static com.agnux.tms.api.handler.ServiceResponseHelper.unauthorized;

import com.agnux.tms.errors.TmsException;
import com.agnux.tms.errors.ErrorCodes;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@AllArgsConstructor
public abstract class AbstractRoleFilter implements HandlerFilterFunction<ServerResponse, ServerResponse> {

    protected final Set<String> requiredRoles;

    protected static final String E_MSG_CONTEXT = "Role filter";

    protected abstract List<String> extractUserRoles(Jwt jwt);

    @Override
    public Mono<ServerResponse> filter(ServerRequest request, HandlerFunction<ServerResponse> next) {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> (Jwt) ctx.getAuthentication().getPrincipal())
                .flatMap(jwt -> {
                    List<String> userRoles = extractUserRoles(jwt);

                    if (userRoles == null || userRoles.isEmpty()) {
                        final String errorMsg = "roles not found in token";
                        log.debug(errorMsg);
                        return forbidden(E_MSG_CONTEXT, new TmsException(errorMsg , ErrorCodes.LACK_OF_DATA_INTEGRITY));
                    }

                    if (userRoles.stream().noneMatch(requiredRoles::contains)) {
                        final String errorMsg = "missing required role";
                        log.debug(errorMsg);
                        return unauthorized(E_MSG_CONTEXT, new TmsException(errorMsg, ErrorCodes.LACK_OF_PERMISSIONS));
                    }

                    return next.handle(request);
                });
    }
}
