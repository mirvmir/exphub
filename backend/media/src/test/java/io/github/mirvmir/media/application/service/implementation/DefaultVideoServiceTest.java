package io.github.mirvmir.media.application.service.implementation;

import io.github.mirvmir.media.application.service.interfaces.VideoAssetService;
import io.github.mirvmir.media.web.request.UploadVideoRequest;
import io.github.mirvmir.media.web.response.UploadedVideoResponse;
import io.github.mirvmir.media.web.response.VideoAssetResponse;
import io.github.mirvmir.media.web.response.VideoPlaybackUrlResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

class DefaultVideoServiceTest {

    private VideoAssetService videoAssetService;
    private DefaultVideoService service;

    @BeforeEach
    void setUp() {
        videoAssetService = mock(VideoAssetService.class);
        service = new DefaultVideoService(videoAssetService);
    }

    @Test
    void uploadVideo_shouldDelegateToVideoAssetService() {
        UploadVideoRequest request = new UploadVideoRequest(
                new MockMultipartFile("file", "lesson.mp4", "video/mp4", "content".getBytes()),
                1L,
                120
        );
        UploadedVideoResponse expected = mock(UploadedVideoResponse.class);

        when(videoAssetService.uploadVideo(request)).thenReturn(expected);

        UploadedVideoResponse result = service.uploadVideo(request);

        assertSame(expected, result);
        verify(videoAssetService).uploadVideo(request);
    }

    @Test
    void getVideoInfo_shouldDelegateToVideoAssetService() {
        VideoAssetResponse expected = mock(VideoAssetResponse.class);

        when(videoAssetService.getVideoInfo(10L)).thenReturn(expected);

        VideoAssetResponse result = service.getVideoInfo(10L);

        assertSame(expected, result);
        verify(videoAssetService).getVideoInfo(10L);
    }

    @Test
    void createPlaybackUrl_shouldDelegateToVideoAssetService() {
        VideoPlaybackUrlResponse expected = mock(VideoPlaybackUrlResponse.class);

        when(videoAssetService.createPlaybackUrl(10L)).thenReturn(expected);

        VideoPlaybackUrlResponse result = service.createPlaybackUrl(10L);

        assertSame(expected, result);
        verify(videoAssetService).createPlaybackUrl(10L);
    }

    @Test
    void streamVideo_shouldDelegateToVideoAssetService() {
        ResponseEntity<Resource> expected = ResponseEntity.ok(new ByteArrayResource("content".getBytes()));

        when(videoAssetService.streamVideo(10L, "token", "bytes=0-3")).thenReturn(expected);

        ResponseEntity<Resource> result = service.streamVideo(10L, "token", "bytes=0-3");

        assertSame(expected, result);
        verify(videoAssetService).streamVideo(10L, "token", "bytes=0-3");
    }
}
