package io.github.mirvmir.media.application.service.port.repository;

import io.github.mirvmir.media.domain.VideoAsset;

import java.util.Set;

public interface VideoAssetRepository {
    VideoAsset findById(Long id);
    VideoAsset save(VideoAsset videoAsset);
    long countExistingByIds(Set<Long> ids);
}
