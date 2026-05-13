package io.github.mirvmir.enrollment.application.persistence.mapper;

import io.github.mirvmir.enrollment.domain.order.Order;
import io.github.mirvmir.enrollment.application.persistence.entity.OrderEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    OrderEntity toEntity(Order order);

    default Order toDomain(OrderEntity entity) {
        if (entity == null) {
            return null;
        }

        return Order.load(
                entity.getId(),
                entity.getUserId(),
                entity.getEnrollmentId(),
                entity.getTargetType(),
                entity.getTargetId(),
                entity.getTargetVersionId(),
                entity.getAmount(),
                entity.getCurrency(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getExpiresAt()
        );
    }
}