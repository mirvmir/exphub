package io.github.mirvmir.wallet.application.service.port.repository;

import io.github.mirvmir.wallet.domain.WalletTransaction;
import io.github.mirvmir.wallet.domain.WalletTransactionType;

public interface WalletTransactionRepository {
    WalletTransaction save(WalletTransaction transaction);
    boolean existsByPaymentId(Long paymentId, WalletTransactionType type);
    boolean existsByWalletWithdrawalId(Long walletWithdrawalId, WalletTransactionType type);
}
