package io.github.mirvmir.enrollment.application.properties;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class BookingProperties {

    @Value("${booking.expires-minutes}")
    private long bookingExpiresMinutes;
}