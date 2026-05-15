package io.github.mirvmir.media.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import static lombok.AccessLevel.PRIVATE;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MediaFileAsset {
    private Long id;
    @NonNull
    private String originalName;
    @NonNull
    private String mimeType;
    @NonNull
    private Long sizeBytes;
    @NonNull
    private String storagePath;

    public static MediaFileAsset create(
            String originalName,
            String mimeType,
            Long sizeBytes,
            String storagePath
    ) {
        return new MediaFileAsset(
                null,
                originalName,
                mimeType,
                sizeBytes,
                storagePath
        );
    }

    public static MediaFileAsset load(
            Long id,
            String originalName,
            String mimeType,
            Long sizeBytes,
            String storagePath
    ) {
        return new MediaFileAsset(
                id,
                originalName,
                mimeType,
                sizeBytes,
                storagePath
        );
    }

    public void assignId(Long id) {
        this.id = id;
    }
}