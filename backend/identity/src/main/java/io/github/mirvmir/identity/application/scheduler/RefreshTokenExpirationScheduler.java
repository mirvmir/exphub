package io.github.mirvmir.identity.application.scheduler;

import io.github.mirvmir.identity.application.service.interfaces.RefreshService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@AllArgsConstructor
@Component
public class RefreshTokenExpirationScheduler {

    private final RefreshService refreshService;

    @Scheduled(
            cron = "0 0 2 * * *",
            zone = "Europe/Moscow"
    )
    public void expireOrders() {
        log.debug("Running refresh token expiration");
        refreshService.deleteExpiredRefreshTokens();
    }
}