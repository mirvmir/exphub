package io.github.mirvmir.payment.application.service.port.event;

import io.github.mirvmir.payment.api.event.PaymentRefundedEvent;
import io.github.mirvmir.payment.api.event.PaymentSucceededEvent;
import io.github.mirvmir.payment.api.event.PayoutSucceededEvent;

public interface PaymentEventPublisher {
    void publishPaymentSucceeded(PaymentSucceededEvent event);
    void publishPayoutSucceeded(PayoutSucceededEvent event);
    void publishPaymentRefunded(PaymentRefundedEvent event);
}
