package io.github.mirvmir.media.domain;

public record StoredFile(
        String originalName,
        String mimeType,
        Long sizeBytes,
        String storagePath
) {
}