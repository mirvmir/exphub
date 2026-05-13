package io.github.mirvmir.payment.application.persistence.mapper;

import io.github.mirvmir.payment.domain.Payment;
import io.github.mirvmir.payment.application.persistence.entity.PaymentEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    default Payment toDomain(PaymentEntity entity) {
        if (entity == null) {
            return null;
        }

        return Payment.load(
                entity.getId(),
                entity.getUserId(),
                entity.getExternalPaymentId(),
                entity.getPrice().getAmount(),
                entity.getPrice().getCurrency(),
                entity.getStatus(),
                entity.getDescription(),
                entity.getOrderId(),
                entity.getCreatedAt(),
                entity.getPaidAt()
        );
    }

    PaymentEntity toEntity(Payment payment);
}
