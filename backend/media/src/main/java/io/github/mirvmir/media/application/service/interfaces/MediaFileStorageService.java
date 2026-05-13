package io.github.mirvmir.media.application.service.interfaces;

import io.github.mirvmir.media.domain.StoredFile;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface MediaFileStorageService {
    StoredFile save(MultipartFile file);
    Resource loadAsResource(String storagePath);
}