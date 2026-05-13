package io.github.mirvmir.payment.application.service.port.repository;

import io.github.mirvmir.payment.domain.UserCard;

import java.util.List;

public interface UserCardRepository {
    UserCard save(UserCard card);
    UserCard findById(Long id);
    List<UserCard> findByUserId(Long userId);
    boolean existsByUserIdAndCardToken(Long userId, String cardToken);
    UserCard findDefaultByUserId(Long userId);
    boolean existsByUserId(Long userId);
}
