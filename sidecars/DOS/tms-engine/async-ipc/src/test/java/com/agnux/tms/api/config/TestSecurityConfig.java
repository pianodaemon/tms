package com.agnux.tms.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Configuration
@Profile("test")
public class TestSecurityConfig {

    private static final String FAKE_TENANT_ID = "0e4a2f68-1a9a-474e-872f-b8afb3a9b275";
    private static final String FAKE_TOKEN = "fake-token";

    public String getFakeToken() {
        return String.format("Bearer %s", FAKE_TOKEN);
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges.anyExchange().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtDecoder(jwtDecoder()))
                )
                .build();
    }

    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        return token -> {
            if (!FAKE_TOKEN.equals(token)) {
                return Mono.error(new RuntimeException("Invalid token"));
            }

            Map<String, Object> claims = new HashMap<>();
            claims.put("sub", "test-user");
            claims.put("scope", "ROLE_ADMIN");
            claims.put("tenantId", "0e4a2f68-1a9a-474e-872f-b8afb3a9b275");

            Jwt jwt = new Jwt(
                    token,
                    Instant.now(),
                    Instant.now().plusSeconds(3600),
                    Map.of("alg", "none"),
                    claims
            );

            return Mono.just(jwt);
        };
    }
}
