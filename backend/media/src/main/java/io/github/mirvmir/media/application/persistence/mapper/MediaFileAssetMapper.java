package io.github.mirvmir.media.application.persistence.mapper;

import io.github.mirvmir.media.domain.MediaFileAsset;
import io.github.mirvmir.media.application.persistence.entity.MediaFileAssetEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MediaFileAssetMapper {

    default MediaFileAssetEntity toEntity(MediaFileAsset mediaFileAsset) {
        if (mediaFileAsset == null) {
            return null;
        }

        MediaFileAssetEntity entity = new MediaFileAssetEntity();
        entity.setId(mediaFileAsset.getId());
        entity.setOriginalName(mediaFileAsset.getOriginalName());
        entity.setMimeType(mediaFileAsset.getMimeType());
        entity.setSizeBytes(mediaFileAsset.getSizeBytes());
        entity.setStoragePath(mediaFileAsset.getStoragePath());

        return entity;
    }

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