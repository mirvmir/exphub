package io.github.mirvmir.enrollment.application.service.interfaces;

import java.time.Instant;

public interface EnrollmentService {
    void markPayed(Long orderId, Instant now);
    void markRefunded(Long orderId);
}
