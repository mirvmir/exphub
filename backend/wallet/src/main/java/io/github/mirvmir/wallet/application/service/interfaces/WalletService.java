package io.github.mirvmir.wallet.application.service.interfaces;

import io.github.mirvmir.payment.api.event.PayoutSucceededEvent;
import io.github.mirvmir.wallet.web.request.WithdrawWalletRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;

public interface WalletService {
    void accrueForPayment(Long paymentId,
                          Long orderId,
                          Currency currency,
                          BigDecimal amount);
    void withdrawToCard(WithdrawWalletRequest request);
    void completeWithdrawal(Long payoutId,
                            Long walletWithdrawalId,
                            Currency currency,
                            BigDecimal amount,
                            Instant paidAt);
}