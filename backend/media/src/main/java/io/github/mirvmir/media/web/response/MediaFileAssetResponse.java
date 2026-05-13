package io.github.mirvmir.media.web.response;

import io.github.mirvmir.media.domain.MediaFileType;


public record MediaFileAssetResponse(
        Long id,
        String originalName,
        String mimeType,
        Long sizeBytes,
        MediaFileType fileType
) {
}