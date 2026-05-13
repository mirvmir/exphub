package io.github.mirvmir.wallet.application.persistence.entity;

import io.github.mirvmir.common.domain.Money;
import io.github.mirvmir.wallet.domain.WalletWithdrawalStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "wallet_withdrawal")
public class WalletWithdrawalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "wallet_id", nullable = false)
    private Long walletId;

    @Embedded
    private Money price;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private WalletWithdrawalStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;
}
