package io.github.mirvmir.media.application.persistence.mapper;

import io.github.mirvmir.media.domain.MediaFileAsset;
import io.github.mirvmir.media.application.persistence.entity.MediaFileAssetEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MediaFileAssetMapper {

    MediaFileAssetEntity toEntity(MediaFileAsset mediaFileAsset);

    default MediaFileAsset toDomain(MediaFileAssetEntity entity) {
        if (entity == null) {
            return null;
        }

        return MediaFileAsset.load(
                entity.getId(),
                entity.getOriginalName(),
                entity.getMimeType(),
                entity.getSizeBytes(),
                entity.getStoragePath()
        );
    }
}