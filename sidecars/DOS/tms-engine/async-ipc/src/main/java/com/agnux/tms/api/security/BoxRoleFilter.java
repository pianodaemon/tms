package com.agnux.tms.api.security;

import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class BoxRoleFilter extends CognitoRoleFilter {

    public BoxRoleFilter() {
        super(Set.of("Admin"));
    }
}
