package io.github.mirvmir.media.web.controller;

import io.github.mirvmir.media.application.service.interfaces.VideoService;
import io.github.mirvmir.media.web.request.UploadVideoRequest;
import io.github.mirvmir.media.web.response.UploadedVideoResponse;
import io.github.mirvmir.media.web.response.VideoAssetResponse;
import io.github.mirvmir.media.web.response.VideoPlaybackUrlResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/media/videos")
@AllArgsConstructor
@Validated
public class VideoController {

    private final VideoService videoService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadedVideoResponse uploadVideo(
            @Valid
            @ModelAttribute
            UploadVideoRequest request
    ) {
        return videoService.uploadVideo(request);
    }

    @GetMapping("/{videoId}")
    public VideoAssetResponse getVideoInfo(
            @PathVariable("videoId")
            Long videoId
    ) {
        return videoService.getVideoInfo(videoId);
    }

    @PostMapping("/{videoId}/playback-token")
    public VideoPlaybackUrlResponse createPlaybackUrl(
            @PathVariable("videoId")
            Long videoId
    ) {
        return videoService.createPlaybackUrl(videoId);
    }

    @GetMapping("/{videoId}/stream")
    public ResponseEntity<Resource> streamVideo(
            @PathVariable("videoId")
            Long videoId,
            @RequestParam("token")
            String token,
            @RequestHeader(value = HttpHeaders.RANGE, required = false)
            String rangeHeader
    ) {
        return videoService.streamVideo(videoId, token, rangeHeader);
    }
}