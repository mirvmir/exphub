package io.github.mirvmir.media.web.controller;

import io.github.mirvmir.common.exception.GlobalExceptionHandler;
import io.github.mirvmir.media.application.service.interfaces.MediaFileService;
import io.github.mirvmir.media.application.service.interfaces.VideoService;
import io.github.mirvmir.media.domain.MediaFileType;
import io.github.mirvmir.media.web.response.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MediaControllersTest {

    private MediaFileService mediaFileService;
    private VideoService videoService;

    private MockMvc mediaFileMockMvc;
    private MockMvc videoMockMvc;

    @BeforeEach
    void setUp() {
        mediaFileService = mock(MediaFileService.class);
        videoService = mock(VideoService.class);

        mediaFileMockMvc = MockMvcBuilders
                .standaloneSetup(new MediaFileController(mediaFileService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        videoMockMvc = MockMvcBuilders
                .standaloneSetup(new VideoController(videoService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void uploadFile_shouldReturn200() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                "image/png",
                "image-content".getBytes()
        );
        UploadedMediaFileResponse response = new UploadedMediaFileResponse(
                1L,
                "/media/files/1/content"
        );

        when(mediaFileService.uploadFile(any())).thenReturn(response);

        mediaFileMockMvc.perform(multipart("/media/files")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.contentUrl").value("/media/files/1/content"));

        verify(mediaFileService).uploadFile(argThat(request ->
                request != null
                        && request.file() != null
                        && request.file().getOriginalFilename().equals("avatar.png")
        ));
    }

    @Test
    void getFileInfo_shouldReturn200() throws Exception {
        MediaFileAssetResponse response = new MediaFileAssetResponse(
                1L,
                "lesson.pdf",
                "application/pdf",
                100L,
                MediaFileType.DOCUMENT
        );

        when(mediaFileService.getFileInfo(1L)).thenReturn(response);

        mediaFileMockMvc.perform(get("/media/files/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.originalName").value("lesson.pdf"))
                .andExpect(jsonPath("$.fileType").value("DOCUMENT"));

        verify(mediaFileService).getFileInfo(1L);
    }

    @Test
    void createAccessUrl_shouldReturn200() throws Exception {
        MediaFileAccessUrlResponse response = new MediaFileAccessUrlResponse(
                "/media/files/1/content?token=file-token"
        );

        when(mediaFileService.createAccessUrl(1L)).thenReturn(response);

        mediaFileMockMvc.perform(post("/media/files/1/access-token"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.contentUrl").value("/media/files/1/content?token=file-token"));

        verify(mediaFileService).createAccessUrl(1L);
    }

    @Test
    void getContent_shouldReturnFileContent() throws Exception {
        Resource resource = new ByteArrayResource("pdf-content".getBytes());

        when(mediaFileService.getContent(1L, "file-token"))
                .thenReturn(ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .contentLength(11L)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=lesson.pdf")
                        .body(resource));

        mediaFileMockMvc.perform(get("/media/files/1/content")
                        .param("token", "file-token"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE))
                .andExpect(header().longValue(HttpHeaders.CONTENT_LENGTH, 11L))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=lesson.pdf"));

        verify(mediaFileService).getContent(1L, "file-token");
    }

    @Test
    void getPublicContent_shouldReturnImageContent() throws Exception {
        Resource resource = new ByteArrayResource("image-content".getBytes());
        ResponseEntity<Resource> response = ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .contentLength(13L)
                .body(resource);

        when(mediaFileService.getPublicContent(1L)).thenReturn(response);

        mediaFileMockMvc.perform(get("/media/files/1/public-content"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_PNG_VALUE))
                .andExpect(header().longValue(HttpHeaders.CONTENT_LENGTH, 13L));

        verify(mediaFileService).getPublicContent(1L);
    }

    @Test
    void getPublicContent_whenForbidden_shouldReturn403() throws Exception {
        when(mediaFileService.getPublicContent(1L))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN));

        mediaFileMockMvc.perform(get("/media/files/1/public-content"))
                .andExpect(status().isForbidden());

        verify(mediaFileService).getPublicContent(1L);
    }

    @Test
    void uploadVideo_shouldReturn200() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "lesson.mp4",
                "video/mp4",
                "video-content".getBytes()
        );
        UploadedVideoResponse response = new UploadedVideoResponse(
                10L,
                "/media/videos/10/stream"
        );

        when(videoService.uploadVideo(any())).thenReturn(response);

        videoMockMvc.perform(multipart("/media/videos")
                        .file(file)
                        .param("posterFileId", "1")
                        .param("durationSeconds", "120"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.streamUrl").value("/media/videos/10/stream"));

        verify(videoService).uploadVideo(argThat(request ->
                request != null
                        && request.file().getOriginalFilename().equals("lesson.mp4")
                        && request.posterFileId().equals(1L)
                        && request.durationSeconds().equals(120)
        ));
    }

    @Test
    void getVideoInfo_shouldReturn200() throws Exception {
        VideoAssetResponse response = new VideoAssetResponse(
                10L,
                1L,
                120,
                null,
                "/media/files/1/content"
        );

        when(videoService.getVideoInfo(10L)).thenReturn(response);

        videoMockMvc.perform(get("/media/videos/10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.posterFileId").value(1L))
                .andExpect(jsonPath("$.durationSeconds").value(120));

        verify(videoService).getVideoInfo(10L);
    }

    @Test
    void createPlaybackUrl_shouldReturn200() throws Exception {
        VideoPlaybackUrlResponse response = new VideoPlaybackUrlResponse(
                "/media/videos/10/stream?token=video-token"
        );

        when(videoService.createPlaybackUrl(10L)).thenReturn(response);

        videoMockMvc.perform(post("/media/videos/10/playback-token"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.streamUrl").value("/media/videos/10/stream?token=video-token"));

        verify(videoService).createPlaybackUrl(10L);
    }

    @Test
    void streamVideo_withoutRange_shouldReturn200() throws Exception {
        Resource resource = new ByteArrayResource("0123456789".getBytes());
        ResponseEntity<Resource> response = ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("video/mp4"))
                .contentLength(10L)
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .header(HttpHeaders.CONTENT_RANGE, "bytes 0-9/10")
                .body(resource);

        when(videoService.streamVideo(10L, "video-token", null)).thenReturn(response);

        videoMockMvc.perform(get("/media/videos/10/stream")
                        .param("token", "video-token"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "video/mp4"))
                .andExpect(header().longValue(HttpHeaders.CONTENT_LENGTH, 10L))
                .andExpect(header().string(HttpHeaders.ACCEPT_RANGES, "bytes"))
                .andExpect(header().string(HttpHeaders.CONTENT_RANGE, "bytes 0-9/10"));

        verify(videoService).streamVideo(10L, "video-token", null);
    }

    @Test
    void streamVideo_withRange_shouldReturn206() throws Exception {
        Resource resource = new ByteArrayResource("2345".getBytes());
        ResponseEntity<Resource> response = ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .contentType(MediaType.parseMediaType("video/mp4"))
                .contentLength(4L)
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .header(HttpHeaders.CONTENT_RANGE, "bytes 2-5/10")
                .body(resource);

        when(videoService.streamVideo(10L, "video-token", "bytes=2-5"))
                .thenReturn(response);

        videoMockMvc.perform(get("/media/videos/10/stream")
                        .param("token", "video-token")
                        .header(HttpHeaders.RANGE, "bytes=2-5"))
                .andExpect(status().isPartialContent())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "video/mp4"))
                .andExpect(header().longValue(HttpHeaders.CONTENT_LENGTH, 4L))
                .andExpect(header().string(HttpHeaders.ACCEPT_RANGES, "bytes"))
                .andExpect(header().string(HttpHeaders.CONTENT_RANGE, "bytes 2-5/10"));

        verify(videoService).streamVideo(10L, "video-token", "bytes=2-5");
    }
}
