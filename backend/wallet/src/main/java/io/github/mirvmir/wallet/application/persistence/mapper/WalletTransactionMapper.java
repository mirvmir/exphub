package io.github.mirvmir.wallet.application.persistence.mapper;

import io.github.mirvmir.wallet.domain.WalletTransaction;
import io.github.mirvmir.wallet.application.persistence.entity.WalletTransactionEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WalletTransactionMapper {

    WalletTransactionEntity toEntity(WalletTransaction wallet);

    default WalletTransaction toDomain(WalletTransactionEntity entity) {
        if (entity == null) {
            return null;
        }

        return WalletTransaction.load(
                entity.getId(),
                entity.getWalletId(),
                entity.getPaymentId(),
                entity.getWalletWithdrawalId(),
                entity.getPrice(),
                entity.getType(),
                entity.getCreatedAt()
        );
    }
}
