package io.github.mirvmir.enrollment.application.event;

import io.github.mirvmir.enrollment.application.service.interfaces.EnrollmentService;
import io.github.mirvmir.payment.api.event.PaymentRefundedEvent;
import io.github.mirvmir.payment.api.event.PaymentSucceededEvent;
import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class EnrollmentEventListener {

    private final EnrollmentService enrollmentService;

    @EventListener
    public void handle(PaymentSucceededEvent event) {
        enrollmentService.markPayed(
                event.orderId(),
                event.paidAt()
        );
    }

    @EventListener
    public void handle(PaymentRefundedEvent event) {
        enrollmentService.markRefunded(event.orderId());
    }
}