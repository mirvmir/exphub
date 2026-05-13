package io.github.mirvmir.payment.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.time.Instant;

import static lombok.AccessLevel.PRIVATE;

@AllArgsConstructor(access = PRIVATE)
@Getter
public class UserCard {
    private Long id;
    @NonNull
    private Long userId;
    @NonNull
    private String bankCardId;
    @NonNull
    private String cardToken;
    @NonNull
    private String maskedPan;
    @NonNull
    private String last4;
    @NonNull
    private String paymentSystem;
    private boolean active;
    private boolean defaultCard;
    @NonNull
    private Instant createdAt;
    @NonNull
    private Instant updatedAt;

    public static UserCard createBoundCard(
            Long userId,
            String bankCardId,
            String cardToken,
            String maskedPan,
            String last4,
            String paymentSystem,
            boolean defaultCard,
            Instant now
    ) {
        return new UserCard(
                null,
                userId,
                bankCardId,
                cardToken,
                maskedPan,
                last4,
                paymentSystem,
                true,
                defaultCard,
                now,
                now
        );
    }

    public static UserCard load(
            Long id,
            Long userId,
            String bankCardId,
            String cardToken,
            String maskedPan,
            String last4,
            String paymentSystem,
            boolean active,
            boolean defaultCard,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new UserCard(
                id,
                userId,
                bankCardId,
                cardToken,
                maskedPan,
                last4,
                paymentSystem,
                active,
                defaultCard,
                createdAt,
                updatedAt
        );
    }
}