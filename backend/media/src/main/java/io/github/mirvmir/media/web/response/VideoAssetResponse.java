package io.github.mirvmir.media.web.response;

public record VideoAssetResponse(
        Long id,
        Long posterFileId,
        Integer durationSeconds,
        String streamUrl,
        String posterUrl
) {
}
