package io.github.mirvmir.media.application.service.port.repository;

import io.github.mirvmir.media.domain.MediaFileAsset;

import java.util.Set;

public interface MediaFileAssetRepository {
    MediaFileAsset findById(Long id);
    MediaFileAsset save(MediaFileAsset mediaFileAsset);
    long countExistingByIds(Set<Long> ids);
}
