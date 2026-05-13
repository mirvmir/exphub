package io.github.mirvmir.media.application.service.port.repository;

import io.github.mirvmir.media.domain.VideoAsset;

public interface VideoAssetRepository {
    VideoAsset findById(Long id);
    VideoAsset save(VideoAsset videoAsset);
}
