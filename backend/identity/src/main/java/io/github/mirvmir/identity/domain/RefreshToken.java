package io.github.mirvmir.identity.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.Instant;

import static lombok.AccessLevel.PROTECTED;

@NoArgsConstructor(access = PROTECTED)
@Entity
@Table(name = "refresh_token")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // У пользователя может быть несколько сессий
    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = false)
    private User user;

    @Column(name = "token_hash", nullable = false)
    private String tokenHash;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Getter
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    public RefreshToken(User user, String tokenHash, Duration expiration) {
        this.user = user;
        this.tokenHash = tokenHash;

        Instant now = Instant.now();
        this.createdAt = Instant.from(now);
        this.expiresAt = Instant.from(now.plus(expiration));
    }
}
