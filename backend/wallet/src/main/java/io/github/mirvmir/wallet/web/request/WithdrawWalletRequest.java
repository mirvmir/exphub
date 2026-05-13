package io.github.mirvmir.wallet.web.request;

import java.math.BigDecimal;
import java.util.Currency;

public record WithdrawWalletRequest(
        Long cardId,
        BigDecimal amount,
        Currency currency
) {
}