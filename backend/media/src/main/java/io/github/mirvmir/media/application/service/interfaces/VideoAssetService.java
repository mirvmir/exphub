package io.github.mirvmir.media.application.service.interfaces;

import io.github.mirvmir.media.domain.VideoAsset;
import io.github.mirvmir.media.web.request.UploadVideoRequest;
import io.github.mirvmir.media.web.response.UploadedVideoResponse;
import io.github.mirvmir.media.web.response.VideoAssetResponse;
import io.github.mirvmir.media.web.response.VideoPlaybackUrlResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

public interface VideoAssetService {
    UploadedVideoResponse uploadVideo(UploadVideoRequest request);
    VideoAssetResponse getVideoInfo(Long videoId);
    VideoPlaybackUrlResponse createPlaybackUrl(Long videoId);
    ResponseEntity<Resource> streamVideo(Long videoId,
                                         String token,
                                         String rangeHeader);
    VideoAsset getExistingVideo(Long videoId);
}