package io.github.mirvmir.media.application.service.interfaces;

import io.github.mirvmir.media.domain.StoredVideo;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface VideoStorageService {
    StoredVideo save(MultipartFile file);
    Resource loadAsResource(String storagePath, long start, long end);
    long size(String storagePath);
}