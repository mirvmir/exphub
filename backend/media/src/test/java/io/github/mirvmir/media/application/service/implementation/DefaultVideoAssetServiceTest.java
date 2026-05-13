package io.github.mirvmir.media.application.service.implementation;

import io.github.mirvmir.common.exception.NotFoundException;
import io.github.mirvmir.common.properties.AppProperties;
import io.github.mirvmir.media.application.properties.VideoStorageProperties;
import io.github.mirvmir.media.application.service.interfaces.VideoAccessService;
import io.github.mirvmir.media.application.service.interfaces.VideoPlaybackTokenService;
import io.github.mirvmir.media.application.service.interfaces.VideoStorageService;
import io.github.mirvmir.media.application.service.port.repository.VideoAssetRepository;
import io.github.mirvmir.media.domain.StoredVideo;
import io.github.mirvmir.media.domain.VideoAsset;
import io.github.mirvmir.media.web.request.UploadVideoRequest;
import io.github.mirvmir.media.web.response.UploadedVideoResponse;
import io.github.mirvmir.media.web.response.VideoAssetResponse;
import io.github.mirvmir.media.web.response.VideoPlaybackUrlResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

class DefaultVideoAssetServiceTest {

    private VideoAssetRepository videoAssetRepository;
    private VideoStorageService videoStorageService;
    private VideoPlaybackTokenService videoPlaybackTokenService;
    private VideoAccessService videoAccessService;

    private DefaultVideoAssetService service;

    @BeforeEach
    void setUp() {
        videoAssetRepository = mock(VideoAssetRepository.class);
        videoStorageService = mock(VideoStorageService.class);
        videoPlaybackTokenService = mock(VideoPlaybackTokenService.class);
        videoAccessService = mock(VideoAccessService.class);

        AppProperties appProperties = mock(AppProperties.class);
        when(appProperties.getAppMode()).thenReturn("web");

        VideoStorageProperties properties = new VideoStorageProperties(
                appProperties,
                "/web/videos",
                "/docker/videos",
                1000L,
                4L
        );

        service = new DefaultVideoAssetService(
                videoAssetRepository,
                videoStorageService,
                videoPlaybackTokenService,
                videoAccessService,
                properties
        );
    }

    @Test
    void uploadVideo_shouldSaveVideoAndReturnStreamUrl() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "lesson.mp4",
                "video/mp4",
                "video-content".getBytes()
        );
        UploadVideoRequest request = new UploadVideoRequest(
                file,
                7L,
                120
        );
        VideoAsset savedVideo = VideoAsset.load(
                10L,
                7L,
                120,
                "videos/lesson.mp4"
        );

        when(videoStorageService.save(file)).thenReturn(new StoredVideo("videos/lesson.mp4"));
        when(videoAssetRepository.save(any(VideoAsset.class))).thenReturn(savedVideo);

        UploadedVideoResponse result = service.uploadVideo(request);

        assertEquals(10L, result.id());
        assertEquals("/media/videos/10/stream", result.streamUrl());
        verify(videoStorageService).save(file);
        verify(videoAssetRepository).save(argThat(video ->
                video.getId() == null
                        && video.getPosterFileId().equals(7L)
                        && video.getDurationSeconds().equals(120)
                        && video.getStoragePath().equals("videos/lesson.mp4")
        ));
    }

    @Test
    void uploadVideo_whenFileIsEmpty_shouldThrowException() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "lesson.mp4",
                "video/mp4",
                new byte[0]
        );
        UploadVideoRequest request = new UploadVideoRequest(
                file,
                null,
                120
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.uploadVideo(request)
        );

        assertEquals("Видео не передано", exception.getMessage());
        verify(videoStorageService, never()).save(any());
        verify(videoAssetRepository, never()).save(any());
    }

    @Test
    void uploadVideo_whenFileIsNotVideo_shouldThrowException() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "lesson.pdf",
                "application/pdf",
                "content".getBytes()
        );
        UploadVideoRequest request = new UploadVideoRequest(
                file,
                null,
                120
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.uploadVideo(request)
        );

        assertEquals("Файл должен быть видео", exception.getMessage());
        verify(videoStorageService, never()).save(any());
        verify(videoAssetRepository, never()).save(any());
    }

    @Test
    void uploadVideo_whenFileTooBig_shouldThrowException() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "lesson.mp4",
                "video/mp4",
                new byte[1001]
        );
        UploadVideoRequest request = new UploadVideoRequest(
                file,
                null,
                120
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.uploadVideo(request)
        );

        assertEquals("Размер видео превышает допустимый лимит", exception.getMessage());
        verify(videoStorageService, never()).save(any());
        verify(videoAssetRepository, never()).save(any());
    }

    @Test
    void getVideoInfo_shouldReturnVideoInfoWithPosterUrl() {
        VideoAsset video = video(10L);

        when(videoAssetRepository.findById(10L)).thenReturn(video);

        VideoAssetResponse result = service.getVideoInfo(10L);

        assertEquals(10L, result.id());
        assertEquals(7L, result.posterFileId());
        assertEquals(120, result.durationSeconds());
        assertNull(result.streamUrl());
        assertEquals("/media/files/7/content", result.posterUrl());
    }

    @Test
    void getVideoInfo_whenVideoNotFound_shouldThrowException() {
        when(videoAssetRepository.findById(10L)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> service.getVideoInfo(10L));
    }

    @Test
    void createPlaybackUrl_shouldCheckAccessCreateTokenAndReturnUrl() {
        VideoAsset video = video(10L);

        when(videoAssetRepository.findById(10L)).thenReturn(video);
        when(videoPlaybackTokenService.createToken(10L)).thenReturn("video-token");

        VideoPlaybackUrlResponse result = service.createPlaybackUrl(10L);

        assertEquals("/media/videos/10/stream?token=video-token", result.streamUrl());
        verify(videoAccessService).checkCurrentUserCanWatch(10L);
        verify(videoPlaybackTokenService).createToken(10L);
    }

    @Test
    void streamVideo_withoutRange_shouldReturnFullFileWith200() {
        VideoAsset video = video(10L);
        Resource resource = new ByteArrayResource("0123456789".getBytes());

        when(videoAssetRepository.findById(10L)).thenReturn(video);
        when(videoStorageService.size("videos/lesson.mp4")).thenReturn(10L);
        when(videoStorageService.loadAsResource("videos/lesson.mp4", 0L, 9L)).thenReturn(resource);

        ResponseEntity<Resource> result = service.streamVideo(
                10L,
                "video-token",
                null
        );

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("video/mp4", result.getHeaders().getContentType().toString());
        assertEquals(10L, result.getHeaders().getContentLength());
        assertEquals("bytes", result.getHeaders().getFirst(HttpHeaders.ACCEPT_RANGES));
        assertEquals("bytes 0-9/10", result.getHeaders().getFirst(HttpHeaders.CONTENT_RANGE));
        assertSame(resource, result.getBody());

        verify(videoPlaybackTokenService).validateToken(10L, "video-token");
        verify(videoStorageService).loadAsResource("videos/lesson.mp4", 0L, 9L);
    }

    @Test
    void streamVideo_withRange_shouldReturnChunkWith206() {
        VideoAsset video = video(10L);
        Resource resource = new ByteArrayResource("2345".getBytes());

        when(videoAssetRepository.findById(10L)).thenReturn(video);
        when(videoStorageService.size("videos/lesson.mp4")).thenReturn(10L);
        when(videoStorageService.loadAsResource("videos/lesson.mp4", 2L, 5L)).thenReturn(resource);

        ResponseEntity<Resource> result = service.streamVideo(
                10L,
                "video-token",
                "bytes=2-5"
        );

        assertEquals(HttpStatus.PARTIAL_CONTENT, result.getStatusCode());
        assertEquals("video/mp4", result.getHeaders().getContentType().toString());
        assertEquals(4L, result.getHeaders().getContentLength());
        assertEquals("bytes", result.getHeaders().getFirst(HttpHeaders.ACCEPT_RANGES));
        assertEquals("bytes 2-5/10", result.getHeaders().getFirst(HttpHeaders.CONTENT_RANGE));
        assertSame(resource, result.getBody());

        verify(videoPlaybackTokenService).validateToken(10L, "video-token");
        verify(videoStorageService).loadAsResource("videos/lesson.mp4", 2L, 5L);
    }

    @Test
    void streamVideo_withOpenEndedRange_shouldUseDefaultChunkSize() {
        VideoAsset video = video(10L);
        Resource resource = new ByteArrayResource("2345".getBytes());

        when(videoAssetRepository.findById(10L)).thenReturn(video);
        when(videoStorageService.size("videos/lesson.mp4")).thenReturn(10L);
        when(videoStorageService.loadAsResource("videos/lesson.mp4", 2L, 5L)).thenReturn(resource);

        ResponseEntity<Resource> result = service.streamVideo(
                10L,
                "video-token",
                "bytes=2-"
        );

        assertEquals(HttpStatus.PARTIAL_CONTENT, result.getStatusCode());
        assertEquals(4L, result.getHeaders().getContentLength());
        assertEquals("bytes 2-5/10", result.getHeaders().getFirst(HttpHeaders.CONTENT_RANGE));

        verify(videoStorageService).loadAsResource("videos/lesson.mp4", 2L, 5L);
    }

    @Test
    void streamVideo_whenRangeEndIsGreaterThanFileSize_shouldThrow416() {
        VideoAsset video = video(10L);

        when(videoAssetRepository.findById(10L)).thenReturn(video);
        when(videoStorageService.size("videos/lesson.mp4")).thenReturn(10L);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.streamVideo(10L, "video-token", "bytes=2-20")
        );

        assertEquals(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE, exception.getStatusCode());
        verify(videoStorageService, never()).loadAsResource(anyString(), anyLong(), anyLong());
    }

    @Test
    void streamVideo_whenRangeStartIsGreaterThanEnd_shouldThrow416() {
        VideoAsset video = video(10L);

        when(videoAssetRepository.findById(10L)).thenReturn(video);
        when(videoStorageService.size("videos/lesson.mp4")).thenReturn(10L);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.streamVideo(10L, "video-token", "bytes=7-2")
        );

        assertEquals(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE, exception.getStatusCode());
        verify(videoStorageService, never()).loadAsResource(anyString(), anyLong(), anyLong());
    }

    private VideoAsset video(Long id) {
        return VideoAsset.load(
                id,
                7L,
                120,
                "videos/lesson.mp4"
        );
    }
}
