package io.github.mirvmir.media.application.service.implementation;

import io.github.mirvmir.common.exception.NotFoundException;
import io.github.mirvmir.common.properties.AppProperties;
import io.github.mirvmir.media.application.properties.MediaFileStorageProperties;
import io.github.mirvmir.media.application.service.interfaces.MediaFileAccessService;
import io.github.mirvmir.media.application.service.interfaces.MediaFileStorageService;
import io.github.mirvmir.media.application.service.interfaces.MediaFileTokenService;
import io.github.mirvmir.media.application.service.mapper.MediaFileTypeResolver;
import io.github.mirvmir.media.application.service.port.repository.MediaFileAssetRepository;
import io.github.mirvmir.media.domain.MediaFileAsset;
import io.github.mirvmir.media.domain.MediaFileType;
import io.github.mirvmir.media.domain.StoredFile;
import io.github.mirvmir.media.web.request.UploadMediaFileRequest;
import io.github.mirvmir.media.web.response.MediaFileAccessUrlResponse;
import io.github.mirvmir.media.web.response.MediaFileAssetResponse;
import io.github.mirvmir.media.web.response.UploadedMediaFileResponse;
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

class DefaultMediaFileServiceTest {

    private MediaFileAssetRepository mediaFileAssetRepository;
    private MediaFileStorageService mediaFileStorageService;
    private MediaFileTokenService mediaFileTokenService;
    private MediaFileAccessService mediaFileAccessService;

    private DefaultMediaFileService service;

    @BeforeEach
    void setUp() {
        mediaFileAssetRepository = mock(MediaFileAssetRepository.class);
        mediaFileStorageService = mock(MediaFileStorageService.class);
        mediaFileTokenService = mock(MediaFileTokenService.class);
        mediaFileAccessService = mock(MediaFileAccessService.class);

        AppProperties appProperties = mock(AppProperties.class);
        when(appProperties.getAppMode()).thenReturn("web");

        MediaFileStorageProperties properties = new MediaFileStorageProperties(
                appProperties,
                "/web/files",
                "/docker/files",
                1000L
        );

        service = new DefaultMediaFileService(
                mediaFileAssetRepository,
                mediaFileStorageService,
                mediaFileTokenService,
                mediaFileAccessService,
                properties,
                new MediaFileTypeResolver()
        );
    }

    @Test
    void uploadFile_shouldSaveFileAndReturnContentUrl() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "lesson.pdf",
                "application/pdf",
                "file-content".getBytes()
        );
        UploadMediaFileRequest request = new UploadMediaFileRequest(file);
        MediaFileAsset savedFile = file(
                1L,
                "lesson.pdf",
                "application/pdf",
                12L,
                "files/lesson.pdf"
        );

        when(mediaFileStorageService.save(file)).thenReturn(new StoredFile(
                "lesson.pdf",
                "application/pdf",
                12L,
                "files/lesson.pdf"
        ));
        when(mediaFileAssetRepository.save(any(MediaFileAsset.class))).thenReturn(savedFile);

        UploadedMediaFileResponse result = service.uploadFile(request);

        assertEquals(1L, result.id());
        assertEquals("/media/files/1/content", result.contentUrl());
        verify(mediaFileStorageService).save(file);
        verify(mediaFileAssetRepository).save(argThat(asset ->
                asset.getId() == null
                        && asset.getOriginalName().equals("lesson.pdf")
                        && asset.getMimeType().equals("application/pdf")
                        && asset.getSizeBytes().equals(12L)
                        && asset.getStoragePath().equals("files/lesson.pdf")
        ));
    }

    @Test
    void uploadFile_whenFileIsEmpty_shouldThrowException() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.pdf",
                "application/pdf",
                new byte[0]
        );
        UploadMediaFileRequest request = new UploadMediaFileRequest(file);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.uploadFile(request)
        );

        assertEquals("Файл не передан", exception.getMessage());
        verify(mediaFileStorageService, never()).save(any());
        verify(mediaFileAssetRepository, never()).save(any());
    }

    @Test
    void uploadFile_whenFileTooBig_shouldThrowException() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "big.pdf",
                "application/pdf",
                new byte[1001]
        );
        UploadMediaFileRequest request = new UploadMediaFileRequest(file);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.uploadFile(request)
        );

        assertEquals("Размер файла превышает допустимый лимит", exception.getMessage());
        verify(mediaFileStorageService, never()).save(any());
        verify(mediaFileAssetRepository, never()).save(any());
    }

    @Test
    void getFileInfo_shouldReturnFileInfo() {
        MediaFileAsset file = pdfFile();

        when(mediaFileAssetRepository.findById(1L)).thenReturn(file);

        MediaFileAssetResponse result = service.getFileInfo(1L);

        assertEquals(1L, result.id());
        assertEquals("lesson.pdf", result.originalName());
        assertEquals("application/pdf", result.mimeType());
        assertEquals(12L, result.sizeBytes());
        assertEquals(MediaFileType.DOCUMENT, result.fileType());
    }

    @Test
    void getFileInfo_whenFileNotFound_shouldThrowException() {
        when(mediaFileAssetRepository.findById(1L)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> service.getFileInfo(1L));
    }

    @Test
    void createAccessUrl_shouldCheckAccessCreateTokenAndReturnUrl() {
        MediaFileAsset file = pdfFile();

        when(mediaFileAssetRepository.findById(1L)).thenReturn(file);
        when(mediaFileTokenService.createToken(1L)).thenReturn("file-token");

        MediaFileAccessUrlResponse result = service.createAccessUrl(1L);

        assertEquals("/media/files/1/content?token=file-token", result.contentUrl());
        verify(mediaFileAccessService).checkCurrentUserCanAccess(1L);
        verify(mediaFileTokenService).createToken(1L);
    }

    @Test
    void getContent_forPdf_shouldReturnInlineContent() {
        MediaFileAsset file = pdfFile();
        Resource resource = new ByteArrayResource("file-content".getBytes());

        when(mediaFileAssetRepository.findById(1L)).thenReturn(file);
        when(mediaFileStorageService.loadAsResource("files/lesson.pdf")).thenReturn(resource);

        ResponseEntity<Resource> result = service.getContent(1L, "file-token");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("application/pdf", result.getHeaders().getContentType().toString());
        assertEquals(12L, result.getHeaders().getContentLength());
        assertTrue(result.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION).startsWith("inline"));
        assertSame(resource, result.getBody());

        verify(mediaFileTokenService).validateToken(1L, "file-token");
        verify(mediaFileStorageService).loadAsResource("files/lesson.pdf");
    }

    @Test
    void getContent_forUnknownBinary_shouldReturnAttachmentContent() {
        MediaFileAsset file = file(
                1L,
                "archive.bin",
                "application/octet-stream",
                10L,
                "files/archive.bin"
        );
        Resource resource = new ByteArrayResource("0123456789".getBytes());

        when(mediaFileAssetRepository.findById(1L)).thenReturn(file);
        when(mediaFileStorageService.loadAsResource("files/archive.bin")).thenReturn(resource);

        ResponseEntity<Resource> result = service.getContent(1L, "file-token");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("application/octet-stream", result.getHeaders().getContentType().toString());
        assertTrue(result.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION).startsWith("attachment"));

        verify(mediaFileTokenService).validateToken(1L, "file-token");
        verify(mediaFileStorageService).loadAsResource("files/archive.bin");
    }

    @Test
    void getPublicContent_forPublicImage_shouldReturnInlineImage() {
        MediaFileAsset file = file(
                2L,
                "avatar.png",
                "image/png",
                13L,
                "files/avatar.png"
        );
        Resource resource = new ByteArrayResource("image-content".getBytes());

        when(mediaFileAssetRepository.findById(2L)).thenReturn(file);
        when(mediaFileStorageService.loadAsResource("files/avatar.png")).thenReturn(resource);

        ResponseEntity<Resource> result = service.getPublicContent(2L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("image/png", result.getHeaders().getContentType().toString());
        assertEquals(13L, result.getHeaders().getContentLength());
        assertEquals("inline", result.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));
        assertSame(resource, result.getBody());

        verify(mediaFileAccessService).checkCanBePublic(2L);
        verify(mediaFileTokenService, never()).validateToken(anyLong(), anyString());
    }

    @Test
    void getPublicContent_forNotImage_shouldThrowForbidden() {
        MediaFileAsset file = pdfFile();

        when(mediaFileAssetRepository.findById(1L)).thenReturn(file);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.getPublicContent(1L)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verify(mediaFileAccessService, never()).checkCanBePublic(anyLong());
        verify(mediaFileStorageService, never()).loadAsResource(anyString());
    }

    private MediaFileAsset pdfFile() {
        return file(
                1L,
                "lesson.pdf",
                "application/pdf",
                12L,
                "files/lesson.pdf"
        );
    }

    private MediaFileAsset file(Long id,
                                String originalName,
                                String mimeType,
                                Long sizeBytes,
                                String storagePath) {
        MediaFileAsset file = MediaFileAsset.create(
                originalName,
                mimeType,
                sizeBytes,
                storagePath
        );
        file.assignId(id);
        return file;
    }
}
