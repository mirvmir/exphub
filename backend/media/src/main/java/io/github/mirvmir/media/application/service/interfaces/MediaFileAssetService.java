package io.github.mirvmir.media.application.service.interfaces;

import io.github.mirvmir.media.domain.MediaFileAsset;
import io.github.mirvmir.media.web.response.MediaFileAssetResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

public interface MediaFileAssetService {
    MediaFileAssetResponse getFileInfo(Long fileId);
    ResponseEntity<Resource> getFileContent(Long fileId);
    MediaFileAsset getExistingFile(Long fileId);
}
