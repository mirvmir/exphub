package io.github.mirvmir.wallet.application.persistence.mapper;

import io.github.mirvmir.wallet.domain.Wallet;
import io.github.mirvmir.wallet.application.persistence.entity.WalletEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WalletMapper {

    WalletEntity toEntity(Wallet wallet);

    default Wallet toDomain(WalletEntity entity) {
        if (entity == null) {
            return null;
        }

        return Wallet.load(
                entity.getId(),
                entity.getTeacherId(),
                entity.getBalance(),
                entity.getCurrency(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}