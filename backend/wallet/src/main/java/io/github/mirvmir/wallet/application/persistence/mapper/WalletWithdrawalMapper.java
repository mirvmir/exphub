package io.github.mirvmir.wallet.application.persistence.mapper;

import io.github.mirvmir.wallet.domain.WalletWithdrawal;
import io.github.mirvmir.wallet.application.persistence.entity.WalletWithdrawalEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WalletWithdrawalMapper {

    default WalletWithdrawal toDomain(WalletWithdrawalEntity entity) {
        if (entity == null) {
            return null;
        }

        return WalletWithdrawal.load(
                entity.getId(),
                entity.getUserId(),
                entity.getWalletId(),
                entity.getPrice().getAmount(),
                entity.getPrice().getCurrency(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getCompletedAt()
        );
    }

    WalletWithdrawalEntity toEntity(WalletWithdrawal withdrawal);
}