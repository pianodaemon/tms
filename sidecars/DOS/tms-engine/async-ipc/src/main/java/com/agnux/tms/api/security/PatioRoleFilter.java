package com.agnux.tms.api.security;

import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class PatioRoleFilter extends CognitoRoleFilter {

    public PatioRoleFilter() {
        super(Set.of("Admin"));
    }
}
