package io.github.mirvmir.identity.application.properties;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class JwtProperties {
    @Value("${jwt.expiration}")
    private String expiration;

    @Value("${jwt.refresh_token.expiration}")
    private String refreshExpiration;

    @Getter
    @Value("${jwt.refresh_token.cleanup.batch_size}")
    private Integer batch;

    @Getter
    @Value("${jwt.refresh_token.cleanup.max_batches}")
    private Integer maxBatch;

    public Duration getExpiration() {
        return Duration.parse(expiration);
    }
    public Duration getRefreshExpiration() {
        return Duration.parse(refreshExpiration);
    }
}