package io.github.mirvmir.media.application.service.implementation;

import io.github.mirvmir.common.exception.NotFoundException;
import io.github.mirvmir.media.application.service.port.repository.VideoAssetRepository;
import io.github.mirvmir.media.application.service.interfaces.VideoAccessService;
import io.github.mirvmir.media.application.service.interfaces.VideoAssetService;
import io.github.mirvmir.media.application.service.interfaces.VideoPlaybackTokenService;
import io.github.mirvmir.media.application.service.interfaces.VideoStorageService;
import io.github.mirvmir.media.domain.StoredVideo;
import io.github.mirvmir.media.domain.VideoAsset;
import io.github.mirvmir.media.exception.MediaErrorCode;
import io.github.mirvmir.media.application.properties.VideoStorageProperties;
import io.github.mirvmir.media.web.request.UploadVideoRequest;
import io.github.mirvmir.media.web.response.UploadedVideoResponse;
import io.github.mirvmir.media.web.response.VideoAssetResponse;
import io.github.mirvmir.media.web.response.VideoPlaybackUrlResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
@AllArgsConstructor
@Slf4j
public class DefaultVideoAssetService implements VideoAssetService {

    private final VideoAssetRepository videoAssetRepository;
    private final VideoStorageService videoStorageService;
    private final VideoPlaybackTokenService videoPlaybackTokenService;
    private final VideoAccessService videoAccessService;
    private final VideoStorageProperties properties;

    @Override
    @Transactional
    public UploadedVideoResponse uploadVideo(UploadVideoRequest request) {
        log.info("Starting uploadVideo");

        try {
            MultipartFile file = request.file();

            validateVideoFile(file);

            StoredVideo storedVideo = videoStorageService.save(file);

            VideoAsset video = VideoAsset.create(
                    request.posterFileId(),
                    request.durationSeconds(),
                    storedVideo.storagePath()
            );

            VideoAsset saved = videoAssetRepository.save(video);

            return new UploadedVideoResponse(
                    saved.getId(),
                    "/media/videos/" + saved.getId() + "/stream"
            );
        
        } catch (Exception e) {
            log.error("Error while uploadVideo", e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public VideoAssetResponse getVideoInfo(Long videoId) {
        log.info("Starting getVideoInfo");

        try {
            VideoAsset video = getExistingVideo(videoId);

            return new VideoAssetResponse(
                    video.getId(),
                    video.getPosterFileId(),
                    video.getDurationSeconds(),
                    null,
                    buildPosterUrl(video.getPosterFileId())
            );
        
        } catch (Exception e) {
            log.error("Error while getVideoInfo", e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public VideoPlaybackUrlResponse createPlaybackUrl(Long videoId) {
        log.info("Starting createPlaybackUrl");

        try {
            VideoAsset video = getExistingVideo(videoId);

            videoAccessService.checkCurrentUserCanWatch(video.getId());

            String token = videoPlaybackTokenService.createToken(video.getId());

            return new VideoPlaybackUrlResponse(
                    "/media/videos/" + video.getId() + "/stream?token=" + token
            );
        
        } catch (Exception e) {
            log.error("Error while createPlaybackUrl", e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<Resource> streamVideo(
            Long videoId,
            String token,
            String rangeHeader
    ) {
        log.info("Starting streamVideo");

        try {
            VideoAsset video = getExistingVideo(videoId);

            videoPlaybackTokenService.validateToken(video.getId(), token);

            long fileSize = videoStorageService.size(video.getStoragePath());

            Range range = rangeHeader == null || rangeHeader.isBlank()
                    ? new Range(0, fileSize - 1)
                    : parseRange(rangeHeader, fileSize);

            Resource resource = videoStorageService.loadAsResource(
                    video.getStoragePath(),
                    range.start(),
                    range.end()
            );

            long contentLength = range.end() - range.start() + 1;

            HttpStatus status = rangeHeader == null || rangeHeader.isBlank()
                    ? HttpStatus.OK
                    : HttpStatus.PARTIAL_CONTENT;

            return ResponseEntity.status(status)
                    .contentType(MediaType.parseMediaType("video/mp4"))
                    .contentLength(contentLength)
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .header(
                            HttpHeaders.CONTENT_RANGE,
                            "bytes " + range.start() + "-" + range.end() + "/" + fileSize
                    )
                    .body(resource);
        
        } catch (Exception e) {
            log.error("Error while streamVideo", e);
            throw e;
        }
    }

    @Override
    public VideoAsset getExistingVideo(Long videoId) {
        log.info("Starting getExistingVideo");

        try {
            VideoAsset video = videoAssetRepository.findById(videoId);

            if (video == null) {
                throw new NotFoundException(
                        MediaErrorCode.VIDEO_NOT_FOUND,
                        "Video with id=" + videoId + " not found"
                );
            }

            return video;
        
        } catch (Exception e) {
            log.error("Error while getExistingVideo", e);
            throw e;
        }
    }

    private void validateVideoFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Видео не передано");
        }

        if (file.getSize() > properties.getMaxSizeBytes()) {
            throw new IllegalArgumentException("Размер видео превышает допустимый лимит");
        }

        String contentType = file.getContentType();

        if (contentType == null || !contentType.startsWith("video/")) {
            throw new IllegalArgumentException("Файл должен быть видео");
        }
    }

    private Range parseRange(String rangeHeader, long fileSize) {
        String value = rangeHeader.replace("bytes=", "");
        String[] parts = value.split("-");

        long start = Long.parseLong(parts[0]);

        long end = parts.length > 1 && !parts[1].isBlank()
                ? Long.parseLong(parts[1])
                : Math.min(start + properties.getDefaultChunkSizeBytes() - 1, fileSize - 1);

        if (start < 0 || end >= fileSize || start > end) {
            throw new ResponseStatusException(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE);
        }

        return new Range(start, end);
    }

    private String buildPosterUrl(Long posterFileId) {
        return posterFileId == null
                ? null
                : "/media/files/" + posterFileId + "/content";
    }

    private record Range(long start, long end) {
    }
}