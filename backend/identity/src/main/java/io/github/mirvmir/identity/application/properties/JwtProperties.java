package io.github.mirvmir.identity.application.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class JwtProperties {
    @Value("${jwt.expiration}")
    private String expiration;

    @Value("${jwt.refresh_token.expiration}")
    private String refreshExpiration;

    public Duration getExpiration() {
        return Duration.parse(expiration);
    }
    public Duration getRefreshExpiration() {
        return Duration.parse(refreshExpiration);
    }
}