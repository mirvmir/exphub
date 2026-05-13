package io.github.mirvmir.wallet.application.persistence.entity;

import io.github.mirvmir.common.domain.Money;
import io.github.mirvmir.wallet.domain.WalletTransactionType;
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
@Table(name = "wallet_transaction")
public class WalletTransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "wallet_id", nullable = false)
    private Long walletId;

    @Column(name = "payment_id")
    private Long paymentId;

    @Column(name = "wallet_withdrawal_id")
    private Long walletWithdrawalId;

    @Embedded
    private Money price;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private WalletTransactionType type;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}