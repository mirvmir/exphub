package io.github.mirvmir.media.application.service.interfaces;

import io.github.mirvmir.media.domain.MediaFileAsset;
import io.github.mirvmir.media.web.request.UploadMediaFileRequest;
import io.github.mirvmir.media.web.response.MediaFileAccessUrlResponse;
import io.github.mirvmir.media.web.response.MediaFileAssetResponse;
import io.github.mirvmir.media.web.response.UploadedMediaFileResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

public interface MediaFileService {
    UploadedMediaFileResponse uploadFile(UploadMediaFileRequest request);
    MediaFileAssetResponse getFileInfo(Long fileId);
    MediaFileAccessUrlResponse createAccessUrl(Long fileId);
    ResponseEntity<Resource> getContent(Long fileId, String token);
    ResponseEntity<Resource> getPublicContent(Long fileId);
    MediaFileAsset getExistingFile(Long fileId);
}