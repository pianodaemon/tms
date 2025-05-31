package com.agnux.tms.api.security;

import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class VehicleRoleFilter extends CognitoRoleFilter {

    public VehicleRoleFilter() {
        super(Set.of("Admin"));
    }
}
