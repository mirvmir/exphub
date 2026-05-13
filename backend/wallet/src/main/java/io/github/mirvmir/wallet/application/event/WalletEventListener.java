package io.github.mirvmir.wallet.application.event;

import io.github.mirvmir.payment.api.event.PaymentSucceededEvent;
import io.github.mirvmir.payment.api.event.PayoutSucceededEvent;
import io.github.mirvmir.wallet.application.service.interfaces.WalletService;
import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class WalletEventListener {

    private final WalletService walletService;

    @EventListener
    public void handle(PaymentSucceededEvent event) {
        walletService.accrueForPayment(
                event.paymentId(),
                event.orderId(),
                event.currency(),
                event.amount()
        );
    }

    @EventListener
    public void handle(PayoutSucceededEvent event) {
        walletService.completeWithdrawal(
                event.payoutId(),
                event.walletWithdrawalId(),
                event.currency(),
                event.amount(),
                event.paidAt()
        );
    }
}