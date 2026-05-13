package io.github.mirvmir.media.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class VideoAsset {

    private Long id;

    private Long posterFileId;

    @NonNull
    private Integer durationSeconds;

    @NonNull
    private String storagePath;

    public static VideoAsset create(
            Long posterFileId,
            Integer durationSeconds,
            String storagePath
    ) {
        return new VideoAsset(
                null,
                posterFileId,
                durationSeconds,
                storagePath
        );
    }

    public static VideoAsset load(
            Long id,
            Long posterFileId,
            Integer durationSeconds,
            String storagePath
    ) {
        return new VideoAsset(
                id,
                posterFileId,
                durationSeconds,
                storagePath
        );
    }

    public void assignId(Long id) {
        this.id = id;
    }
}