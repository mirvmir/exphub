package io.github.mirvmir.wallet.application.event;

import io.github.mirvmir.payment.api.event.PaymentSucceededEvent;
import io.github.mirvmir.payment.api.event.PayoutSucceededEvent;
import io.github.mirvmir.wallet.application.service.interfaces.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;

import static org.mockito.Mockito.*;

class WalletEventListenerTest {

    private WalletService walletService;
    private WalletEventListener listener;

    @BeforeEach
    void setUp() {
        walletService = mock(WalletService.class);
        listener = new WalletEventListener(walletService);
    }

    @Test
    void handlePaymentSucceededEvent_shouldAccrueForPayment() {
        PaymentSucceededEvent event = new PaymentSucceededEvent(
                1L,
                100L,
                2L,
                new BigDecimal("1000"),
                Currency.getInstance("RUB"),
                Instant.parse("2026-05-12T10:00:00Z")
        );

        listener.handle(event);

        verify(walletService).accrueForPayment(
                1L,
                100L,
                Currency.getInstance("RUB"),
                new BigDecimal("1000")
        );
    }

    @Test
    void handlePayoutSucceededEvent_shouldCompleteWithdrawal() {
        Instant paidAt = Instant.parse("2026-05-12T10:00:00Z");

        PayoutSucceededEvent event = new PayoutSucceededEvent(
                30L,
                20L,
                2L,
                new BigDecimal("1500"),
                Currency.getInstance("RUB"),
                paidAt
        );

        listener.handle(event);

        verify(walletService).completeWithdrawal(
                30L,
                20L,
                Currency.getInstance("RUB"),
                new BigDecimal("1500"),
                paidAt
        );
    }
}