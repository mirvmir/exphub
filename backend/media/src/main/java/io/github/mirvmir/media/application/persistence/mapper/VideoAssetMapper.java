package io.github.mirvmir.media.application.persistence.mapper;

import io.github.mirvmir.media.domain.VideoAsset;
import io.github.mirvmir.media.application.persistence.entity.VideoAssetEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VideoAssetMapper {

    VideoAssetEntity toEntity(VideoAsset videoAsset);

    default VideoAsset toDomain(VideoAssetEntity entity) {
        if (entity == null) {
            return null;
        }

        return VideoAsset.load(
                entity.getId(),
                entity.getPosterFileId(),
                entity.getDurationSeconds(),
                entity.getStoragePath()
        );
    }
}