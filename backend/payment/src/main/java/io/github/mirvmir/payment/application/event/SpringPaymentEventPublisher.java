package io.github.mirvmir.payment.application.event;

import io.github.mirvmir.payment.api.event.PaymentRefundedEvent;
import io.github.mirvmir.payment.api.event.PaymentSucceededEvent;
import io.github.mirvmir.payment.api.event.PayoutSucceededEvent;
import io.github.mirvmir.payment.application.service.port.event.PaymentEventPublisher;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class SpringPaymentEventPublisher implements PaymentEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publishPaymentSucceeded(PaymentSucceededEvent event) {
        applicationEventPublisher.publishEvent(event);
    }

    @Override
    public void publishPayoutSucceeded(PayoutSucceededEvent event) {
        applicationEventPublisher.publishEvent(event);
    }

    @Override
    public void publishPaymentRefunded(PaymentRefundedEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
