package com.agnux.tms.api.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import reactor.core.publisher.Mono;
import org.mockito.Mockito;

@TestConfiguration
public class TestJwtDecoderConfig {

    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        Jwt mockJwt = Jwt.withTokenValue("fake-token")
                .header("alg", "none")
                .claim("sub", "test-user")
                .claim("tenantId", "tenant123")
                .build();

        ReactiveJwtDecoder decoder = Mockito.mock(ReactiveJwtDecoder.class);
        Mockito.when(decoder.decode("fake-token")).thenReturn(Mono.just(mockJwt));
        return decoder;
    }
}
