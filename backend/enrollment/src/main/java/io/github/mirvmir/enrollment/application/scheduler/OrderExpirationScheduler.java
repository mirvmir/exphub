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

    @Scheduled(
            cron = "0 0 4 * * *",
            zone = "Europe/Moscow"
    )
    public void expireOrders() {
        log.debug("Running order expiration");
        orderExpirationService.deleteExpiredOrders();
    }
}