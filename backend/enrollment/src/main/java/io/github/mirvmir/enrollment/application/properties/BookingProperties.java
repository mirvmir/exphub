package io.github.mirvmir.enrollment.application.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class BookingProperties {

    @Value("${booking.expires-minutes}")
    private long bookingExpiresMinutes;

    public long getBookingExpiresMinutes() {
        return bookingExpiresMinutes;
    }
}