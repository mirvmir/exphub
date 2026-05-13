package io.github.mirvmir.media.web.response;

public record UploadedVideoResponse(
        Long id,
        String streamUrl
) {
}