package com.agnux.tms.api.security;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Optional;
import java.util.Set;

abstract class CognitoRoleFilter extends AbstractRoleFilter {

    private static final String GROUP_CLAIM = "cognito:groups";

    protected CognitoRoleFilter(Set<String> requiredRoles) {
        super(requiredRoles);
    }

    @Override
    protected List<String> extractUserRoles(Jwt jwt) {
        return Optional.ofNullable(jwt.getClaimAsStringList(GROUP_CLAIM)).orElse(List.of());
    }
}
