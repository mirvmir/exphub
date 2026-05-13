package io.github.mirvmir.payment.application.persistence.mapper;

import io.github.mirvmir.payment.domain.UserCard;
import io.github.mirvmir.payment.application.persistence.entity.UserCardEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserCardMapper {

    default UserCard toDomain(UserCardEntity entity) {
        if (entity == null) {
            return null;
        }

        return UserCard.load(
                entity.getId(),
                entity.getUserId(),
                entity.getBankCardId(),
                entity.getCardToken(),
                entity.getMaskedPan(),
                entity.getLast4(),
                entity.getPaymentSystem(),
                entity.isActive(),
                entity.isDefaultCard(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    UserCardEntity toEntity(UserCard card);
}