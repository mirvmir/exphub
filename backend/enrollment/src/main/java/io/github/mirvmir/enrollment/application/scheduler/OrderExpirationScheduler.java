package io.github.mirvmir.enrollment.application.scheduler;

import io.github.mirvmir.enrollment.application.service.interfaces.OrderExpirationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@AllArgsConstructor
@Component
public class OrderExpirationScheduler {

    private final OrderExpirationService orderExpirationService;

    @Scheduled(fixedDelay = 60_000)
    public void expireOrders() {
        log.debug("Running order expiration");
        orderExpirationService.deleteExpiredOrders();
    }
}