package io.github.mirvmir.media.application.service.implementation;

import io.github.mirvmir.common.exception.NotFoundException;
import io.github.mirvmir.media.application.service.mapper.MediaFileTypeResolver;
import io.github.mirvmir.media.application.service.port.repository.MediaFileAssetRepository;
import io.github.mirvmir.media.application.service.interfaces.MediaFileAssetService;
import io.github.mirvmir.media.application.service.interfaces.MediaFileStorageService;
import io.github.mirvmir.media.domain.MediaFileAsset;
import io.github.mirvmir.media.exception.MediaErrorCode;
import io.github.mirvmir.media.web.response.MediaFileAssetResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

@Service
@AllArgsConstructor
@Slf4j
public class DefaultMediaFileAssetService implements MediaFileAssetService {

    private final MediaFileAssetRepository mediaFileAssetRepository;
    private final MediaFileStorageService mediaFileStorageService;
    private final MediaFileTypeResolver mediaFileTypeResolver;

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
    public ResponseEntity<Resource> getFileContent(Long fileId) {
        log.info("Starting getFileContent");

        try {
            MediaFileAsset file = getExistingFile(fileId);

            Resource resource = mediaFileStorageService.loadAsResource(file.getStoragePath());

            MediaType mediaType = MediaType.parseMediaType(file.getMimeType());

            boolean inline = isInlineContent(file.getMimeType());

            ContentDisposition disposition = inline
                    ? ContentDisposition.inline()
                    .filename(file.getOriginalName(), StandardCharsets.UTF_8)
                    .build()
                    : ContentDisposition.attachment()
                    .filename(file.getOriginalName(), StandardCharsets.UTF_8)
                    .build();

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .contentLength(file.getSizeBytes())
                    .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                    .body(resource);
        
        } catch (Exception e) {
            log.error("Error while getFileContent", e);
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

    private boolean isInlineContent(String mimeType) {
        if (mimeType == null) {
            return false;
        }

        return mimeType.startsWith("image/")
                || mimeType.startsWith("audio/")
                || mimeType.equals("application/pdf")
                || mimeType.startsWith("text/");
    }
}