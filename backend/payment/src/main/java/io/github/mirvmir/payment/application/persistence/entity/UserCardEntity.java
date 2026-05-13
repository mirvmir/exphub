package io.github.mirvmir.payment.application.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

import static lombok.AccessLevel.PROTECTED;

@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
@Getter
@Entity
@Table(name = "user_card")
public class UserCardEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "bank_card_id", nullable = false, unique = true)
    private String bankCardId;

    @Column(name = "card_token", nullable = false, unique = true)
    private String cardToken;

    @Column(name = "masked_pan", nullable = false)
    private String maskedPan;

    @Column(name = "last4", nullable = false, length = 4)
    private String last4;

    @Column(name = "payment_system", nullable = false)
    private String paymentSystem;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "is_default", nullable = false)
    private boolean defaultCard;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}