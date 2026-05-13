package io.github.mirvmir.media.application.service.implementation;

import io.github.mirvmir.common.exception.NotFoundException;
import io.github.mirvmir.media.application.service.mapper.MediaFileTypeResolver;
import io.github.mirvmir.media.application.service.port.repository.MediaFileAssetRepository;
import io.github.mirvmir.media.application.service.interfaces.MediaFileAccessService;
import io.github.mirvmir.media.application.service.interfaces.MediaFileService;
import io.github.mirvmir.media.application.service.interfaces.MediaFileStorageService;
import io.github.mirvmir.media.application.service.interfaces.MediaFileTokenService;
import io.github.mirvmir.media.domain.MediaFileAsset;
import io.github.mirvmir.media.domain.StoredFile;
import io.github.mirvmir.media.exception.MediaErrorCode;
import io.github.mirvmir.media.application.properties.MediaFileStorageProperties;
import io.github.mirvmir.media.web.request.UploadMediaFileRequest;
import io.github.mirvmir.media.web.response.MediaFileAccessUrlResponse;
import io.github.mirvmir.media.web.response.MediaFileAssetResponse;
import io.github.mirvmir.media.web.response.UploadedMediaFileResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;

@Service
@AllArgsConstructor
@Slf4j
public class DefaultMediaFileService implements MediaFileService {

    private final MediaFileAssetRepository mediaFileAssetRepository;
    private final MediaFileStorageService mediaFileStorageService;
    private final MediaFileTokenService mediaFileTokenService;
    private final MediaFileAccessService mediaFileAccessService;
    private final MediaFileStorageProperties properties;
    private final MediaFileTypeResolver mediaFileTypeResolver;

    @Override
    @Transactional
    public UploadedMediaFileResponse uploadFile(UploadMediaFileRequest request) {
        log.info("Starting uploadFile");

        try {
            MultipartFile file = request.file();

            validateFile(file);

            StoredFile storedFile = mediaFileStorageService.save(file);

            MediaFileAsset asset = MediaFileAsset.create(
                    storedFile.originalName(),
                    storedFile.mimeType(),
                    storedFile.sizeBytes(),
                    storedFile.storagePath()
            );

            MediaFileAsset saved = mediaFileAssetRepository.save(asset);

            return new UploadedMediaFileResponse(
                    saved.getId(),
                    "/media/files/" + saved.getId() + "/content"
            );
        
        } catch (Exception e) {
            log.error("Error while uploadFile", e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public MediaFileAssetResponse getFileInfo(Long fileId) {
        log.info("Starting getFileInfo");

        try {
            MediaFileAsset file = getExistingFile(fileId);

            return new MediaFileAssetResponse(
                    file.getId(),
                    file.getOriginalName(),
                    file.getMimeType(),
                    file.getSizeBytes(),
                    mediaFileTypeResolver.resolve(file.getMimeType())
            );
        
        } catch (Exception e) {
            log.error("Error while getFileInfo", e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public MediaFileAccessUrlResponse createAccessUrl(Long fileId) {
        log.info("Starting createAccessUrl");

        try {
            MediaFileAsset file = getExistingFile(fileId);

            mediaFileAccessService.checkCurrentUserCanAccess(file.getId());

            String token = mediaFileTokenService.createToken(file.getId());

            return new MediaFileAccessUrlResponse(
                    "/media/files/" + file.getId() + "/content?token=" + token
            );
        
        } catch (Exception e) {
            log.error("Error while createAccessUrl", e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<Resource> getContent(Long fileId, String token) {
        log.info("Starting getContent");

        try {
            MediaFileAsset file = getExistingFile(fileId);

            mediaFileTokenService.validateToken(file.getId(), token);

            Resource resource = mediaFileStorageService.loadAsResource(file.getStoragePath());

            ContentDisposition disposition = isInline(file.getMimeType())
                    ? ContentDisposition.inline()
                    .filename(file.getOriginalName(), StandardCharsets.UTF_8)
                    .build()
                    : ContentDisposition.attachment()
                    .filename(file.getOriginalName(), StandardCharsets.UTF_8)
                    .build();

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(file.getMimeType()))
                    .contentLength(file.getSizeBytes())
                    .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                    .body(resource);
        
        } catch (Exception e) {
            log.error("Error while getContent", e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<Resource> getPublicContent(Long fileId) {
        log.info("Starting getPublicContent");

        try {
            MediaFileAsset file = getExistingFile(fileId);

            if (!file.getMimeType().startsWith("image/")) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }

            mediaFileAccessService.checkCanBePublic(file.getId());

            Resource resource = mediaFileStorageService.loadAsResource(file.getStoragePath());

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(file.getMimeType()))
                    .contentLength(file.getSizeBytes())
                    .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().build().toString())
                    .body(resource);
        
        } catch (Exception e) {
            log.error("Error while getPublicContent", e);
            throw e;
        }
    }

    @Override
    public MediaFileAsset getExistingFile(Long fileId) {
        log.info("Starting getExistingFile");

        try {
            MediaFileAsset file = mediaFileAssetRepository.findById(fileId);

            if (file == null) {
                throw new NotFoundException(
                        MediaErrorCode.MEDIA_FILE_NOT_FOUND,
                        "Media file with id=" + fileId + " not found"
                );
            }

            return file;
        
        } catch (Exception e) {
            log.error("Error while getExistingFile", e);
            throw e;
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Файл не передан");
        }

        if (file.getSize() > properties.getMaxSizeBytes()) {
            throw new IllegalArgumentException("Размер файла превышает допустимый лимит");
        }
    }

    private boolean isInline(String mimeType) {
        return mimeType != null
                && (mimeType.startsWith("image/")
                || mimeType.startsWith("audio/")
                || mimeType.equals("application/pdf")
                || mimeType.startsWith("text/"));
    }
}