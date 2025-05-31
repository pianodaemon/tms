package com.agnux.tms.api.security;

import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class CustomerRoleFilter  extends CognitoRoleFilter {

    public CustomerRoleFilter() {
        super(Set.of("Admin"));
    }
}
