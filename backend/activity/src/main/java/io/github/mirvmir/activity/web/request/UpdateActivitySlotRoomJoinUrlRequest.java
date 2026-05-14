package io.github.mirvmir.activity.web.request;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

public record UpdateActivitySlotRoomJoinUrlRequest(
        @NotBlank
        @URL(protocol = "https")
        String roomJoinUrl
) {
}