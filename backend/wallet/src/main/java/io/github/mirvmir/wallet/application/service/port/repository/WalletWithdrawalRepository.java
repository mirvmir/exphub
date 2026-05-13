package io.github.mirvmir.wallet.application.service.port.repository;

import io.github.mirvmir.wallet.domain.WalletWithdrawal;

public interface WalletWithdrawalRepository {
    WalletWithdrawal save(WalletWithdrawal withdrawal);
    WalletWithdrawal findById(Long id);
}