package io.github.mirvmir.media.web.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UploadedMediaFileResponse(
        @Positive
        Long id,
        @NotBlank
        @Size(max = 250)
        String contentUrl
) {
}