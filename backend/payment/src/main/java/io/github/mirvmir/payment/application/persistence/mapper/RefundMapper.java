package io.github.mirvmir.payment.application.persistence.mapper;

import io.github.mirvmir.payment.application.persistence.entity.RefundEntity;
import io.github.mirvmir.payment.domain.Refund;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RefundMapper {

    RefundEntity toEntity(Refund refund);

    default Refund toDomain(RefundEntity entity) {
        if (entity == null) {
            return null;
        }

        return Refund.load(
                entity.getId(),
                entity.getPaymentId(),
                entity.getExternalRefundId(),
                entity.getPrice().getAmount(),
                entity.getPrice().getCurrency(),
                entity.getStatus(),
                entity.getReason(),
                entity.getCreatedAt(),
                entity.getRefundedAt()
        );
    }
}