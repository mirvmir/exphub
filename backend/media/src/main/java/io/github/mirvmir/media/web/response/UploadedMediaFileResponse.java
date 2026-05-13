package io.github.mirvmir.media.web.response;

public record UploadedMediaFileResponse(
        Long id,
        String contentUrl
) {
}