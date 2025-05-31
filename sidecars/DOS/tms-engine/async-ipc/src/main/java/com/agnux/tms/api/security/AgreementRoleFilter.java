package com.agnux.tms.api.security;

import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class AgreementRoleFilter extends CognitoRoleFilter {

    public AgreementRoleFilter() {
        super(Set.of("Admin"));
    }
}
