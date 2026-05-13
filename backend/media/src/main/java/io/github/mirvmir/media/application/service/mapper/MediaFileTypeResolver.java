package io.github.mirvmir.media.application.service.mapper;

import io.github.mirvmir.media.domain.MediaFileType;
import org.springframework.stereotype.Component;

@Component
public class MediaFileTypeResolver {

    public MediaFileType resolve(String mimeType) {
        if (mimeType == null) {
            return MediaFileType.OTHER;
        }

        if (mimeType.startsWith("image/")) {
            return MediaFileType.IMAGE;
        }

        if (mimeType.startsWith("video/")) {
            return MediaFileType.VIDEO;
        }

        if (mimeType.startsWith("audio/")) {
            return MediaFileType.AUDIO;
        }

        if (mimeType.equals("application/pdf")
                || mimeType.startsWith("text/")
                || mimeType.contains("word")
                || mimeType.contains("excel")
                || mimeType.contains("powerpoint")) {
            return MediaFileType.DOCUMENT;
        }

        return MediaFileType.OTHER;
    }
}