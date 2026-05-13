package io.github.mirvmir.media.web.request;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public record UploadMediaFileRequest(
        @NotNull(message = "Файл обязателен")
        MultipartFile file
) {
}