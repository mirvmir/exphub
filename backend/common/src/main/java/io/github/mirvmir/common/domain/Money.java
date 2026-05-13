package io.github.mirvmir.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Currency;

import static lombok.AccessLevel.PROTECTED;

@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor
@Getter
@Embeddable
public class Money {

    @Column(name = "price_amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "price_currency", nullable = false)
    private Currency currency = Currency.getInstance("RUB");
}
