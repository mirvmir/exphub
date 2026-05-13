package io.github.mirvmir.identity.application.persistence.mapper;

import io.github.mirvmir.identity.domain.PendingEmailChange;
import io.github.mirvmir.identity.application.persistence.entity.PendingEmailChangeEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PendingEmailChangeMapper {

    PendingEmailChangeEntity toEntity(PendingEmailChange pendingEmailChange);

    default PendingEmailChange toDomain(PendingEmailChangeEntity entity) {
        if (entity == null) {
            return null;
        }

        return PendingEmailChange.load(
                entity.getId(),
                entity.getUserId(),
                entity.getNewEmail(),
                entity.getToken(),
                entity.getExpiresAt()
        );
    }
}