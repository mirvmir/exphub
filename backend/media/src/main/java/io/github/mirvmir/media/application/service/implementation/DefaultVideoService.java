package io.github.mirvmir.media.application.service.implementation;

import io.github.mirvmir.media.application.service.interfaces.VideoAssetService;
import io.github.mirvmir.media.application.service.interfaces.VideoService;
import io.github.mirvmir.media.web.request.UploadVideoRequest;
import io.github.mirvmir.media.web.response.UploadedVideoResponse;
import io.github.mirvmir.media.web.response.VideoAssetResponse;
import io.github.mirvmir.media.web.response.VideoPlaybackUrlResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class DefaultVideoService implements VideoService {

    private final VideoAssetService videoAssetService;

    @Override
    public UploadedVideoResponse uploadVideo(UploadVideoRequest request) {
        log.info("Starting uploadVideo");

        try {
            return videoAssetService.uploadVideo(request);
        
        } catch (Exception e) {
            log.error("Error while uploadVideo", e);
            throw e;
        }
    }

    @Override
    public VideoAssetResponse getVideoInfo(Long videoId) {
        log.info("Starting getVideoInfo");

        try {
            return videoAssetService.getVideoInfo(videoId);
        
        } catch (Exception e) {
            log.error("Error while getVideoInfo", e);
            throw e;
        }
    }

    @Override
    public VideoPlaybackUrlResponse createPlaybackUrl(Long videoId) {
        log.info("Starting createPlaybackUrl");

        try {
            return videoAssetService.createPlaybackUrl(videoId);
        
        } catch (Exception e) {
            log.error("Error while createPlaybackUrl", e);
            throw e;
        }
    }

    @Override
    public ResponseEntity<Resource> streamVideo(
            Long videoId,
            String token,
            String rangeHeader
    ) {
        log.info("Starting streamVideo");

        try {
            return videoAssetService.streamVideo(videoId, token, rangeHeader);
        
        } catch (Exception e) {
            log.error("Error while streamVideo", e);
            throw e;
        }
    }
}