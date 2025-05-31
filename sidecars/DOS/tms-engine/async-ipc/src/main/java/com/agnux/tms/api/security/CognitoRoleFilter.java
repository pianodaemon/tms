package com.agnux.tms.api.security;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Set;

public abstract class CognitoRoleFilter extends AbstractRoleFilter {

    private static final String COGNITO_GROUP_CLAIM = "cognito:groups";

    protected CognitoRoleFilter(Set<String> requiredRoles) {
        super(requiredRoles);
    }

    @Override
    protected List<String> extractUserRoles(Jwt jwt) {
        List<String> result;
        Object rolesObj = jwt.getClaim(COGNITO_GROUP_CLAIM);

        if (rolesObj instanceof List<?> list) {
            result = list.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .toList();
        } else {
            result = List.of();
        }

        return result;
    }
}
