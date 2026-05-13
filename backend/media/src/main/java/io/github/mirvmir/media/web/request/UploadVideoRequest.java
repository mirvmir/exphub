package io.github.mirvmir.media.web.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.web.multipart.MultipartFile;

public record UploadVideoRequest (
        @NotNull(message = "Файл видео обязателен")
        MultipartFile file,
        Long posterFileId,
        @NotNull(message = "Длительность видео обязательна")
        @Positive(message = "Длительность видео должна быть больше 0")
        Integer durationSeconds
) {
}