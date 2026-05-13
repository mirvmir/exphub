package io.github.mirvmir.wallet.application.service.port.repository;

import io.github.mirvmir.wallet.domain.Wallet;

public interface WalletRepository {
    Wallet saveOrUpdate(Wallet wallet);
    Wallet findByUserId(Long teacherId);
}
