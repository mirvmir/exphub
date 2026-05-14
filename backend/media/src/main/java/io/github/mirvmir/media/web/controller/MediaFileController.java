package io.github.mirvmir.media.web.controller;

import io.github.mirvmir.media.application.service.interfaces.MediaFileService;
import io.github.mirvmir.media.web.request.UploadMediaFileRequest;
import io.github.mirvmir.media.web.response.MediaFileAccessUrlResponse;
import io.github.mirvmir.media.web.response.MediaFileAssetResponse;
import io.github.mirvmir.media.web.response.UploadedMediaFileResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/media/files")
@AllArgsConstructor
@Validated
public class MediaFileController {

    private final MediaFileService mediaFileService;

    @PostMapping("/{fileId}/access-token")
    public MediaFileAccessUrlResponse createAccessUrl(
            @PathVariable("fileId")
            Long fileId
    ) {
        return mediaFileService.createAccessUrl(fileId);
    }

    @Validated
    @GetMapping("/{fileId}/content")
    public ResponseEntity<Resource> getContent(
            @PathVariable("fileId")
            Long fileId,
            @NotBlank
            @RequestParam("token")
            String token
    ) {
        return mediaFileService.getContent(fileId, token);
    }

    @GetMapping("/{fileId}")
    public MediaFileAssetResponse getFileInfo(
            @PathVariable("fileId")
            Long fileId
    ) {
        return mediaFileService.getFileInfo(fileId);
    }

    @GetMapping("/{fileId}/public-content")
    public ResponseEntity<Resource> getPublicContent(
            @PathVariable("fileId")
            Long fileId
    ) {
        return mediaFileService.getPublicContent(fileId);
    }


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadedMediaFileResponse uploadFile(
            @Valid
            @ModelAttribute
            UploadMediaFileRequest request
    ) {
        return mediaFileService.uploadFile(request);
    }
}