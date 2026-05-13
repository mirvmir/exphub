package io.github.mirvmir.payment.application.service.port.repository;

import io.github.mirvmir.payment.domain.Payout;

public interface PayoutRepository {
    Payout saveOrUpdate(Payout payout);
    Payout findById(Long id);
    Payout findByExternalPayoutId(String externalPayoutId);
    boolean existsByWalletWithdrawalId(Long walletWithdrawalId);
}
