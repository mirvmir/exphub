package io.github.mirvmir.payment.application.persistence.mapper;

import io.github.mirvmir.payment.domain.Payout;
import io.github.mirvmir.payment.application.persistence.entity.PayoutEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PayoutMapper {

    PayoutEntity toEntity(Payout payout);

    default Payout toDomain(PayoutEntity entity) {
        if (entity == null) {
            return null;
        }

        return Payout.load(
                entity.getId(),
                entity.getUserId(),
                entity.getCardId(),
                entity.getExternalPayoutId(),
                entity.getPrice().getAmount(),
                entity.getPrice().getCurrency(),
                entity.getStatus(),
                entity.getDescription(),
                entity.getWalletWithdrawalId(),
                entity.getCreatedAt(),
                entity.getPaidAt()
        );
    }
}