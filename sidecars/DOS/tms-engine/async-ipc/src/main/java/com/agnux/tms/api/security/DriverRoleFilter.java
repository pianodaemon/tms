package com.agnux.tms.api.security;

import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DriverRoleFilter extends CognitoRoleFilter {

    public DriverRoleFilter() {
        super(Set.of("Admin"));
    }
}
