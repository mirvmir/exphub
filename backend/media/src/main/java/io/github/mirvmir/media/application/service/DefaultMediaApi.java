package io.github.mirvmir.media.application.service;

import io.github.mirvmir.media.api.MediaApi;
import io.github.mirvmir.media.application.service.port.repository.MediaFileAssetRepository;
import io.github.mirvmir.media.application.service.port.repository.VideoAssetRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@AllArgsConstructor
@Service
public class DefaultMediaApi implements MediaApi {

    MediaFileAssetRepository fileRepository;
    VideoAssetRepository videoRepository;

    @Override
    @Transactional(readOnly = true)
    public boolean existsFileById(Long id) {
        return fileRepository.findById(id) != null;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsFileByIds(Set<Long> ids) {
        long existingCount = fileRepository.countExistingByIds(ids);

        return (existingCount == ids.size());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsVideoByIds(Set<Long> ids) {
        long existingCount = videoRepository.countExistingByIds(ids);

        return (existingCount == ids.size());
    }
}
