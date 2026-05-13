package io.github.mirvmir.identity.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

import static lombok.AccessLevel.PRIVATE;

@Getter
@AllArgsConstructor(access = PRIVATE)
public class PendingEmailChange {
    private Long id;
    private Long userId;
    private String newEmail;
    private String token;
    private Instant expiresAt;

    public static PendingEmailChange create(Long userId,
                                            String newEmail,
                                            String token,
                                            Instant expiresAt) {
        return new PendingEmailChange(
                null,
                userId,
                newEmail,
                token,
                expiresAt
        );
    }

    public static PendingEmailChange load(Long id,
                                          Long userId,
                                          String newEmail,
                                          String token,
                                          Instant expiresAt) {
        return new PendingEmailChange(
                id,
                userId,
                newEmail,
                token,
                expiresAt
        );
    }

    public boolean isExpired(Instant now) {
        return !expiresAt.isAfter(now);
    }

    public void assignId(Long id) {
        this.id = id;
    }
}