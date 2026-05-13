package io.github.mirvmir.media.application.service.implementation;

import lombok.extern.slf4j.Slf4j;
import io.github.mirvmir.media.application.service.RangedFileInputStream;
import io.github.mirvmir.media.application.service.interfaces.VideoStorageService;
import io.github.mirvmir.media.domain.StoredVideo;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@Slf4j
public class LocalVideoStorageService implements VideoStorageService {

    private final Path root = Paths.get("uploads/videos");

    @Override
    public StoredVideo save(MultipartFile file) {
        log.info("Starting save");

        try {
            try {
                Files.createDirectories(root);

                String extension = getExtension(file.getOriginalFilename());
                String fileName = UUID.randomUUID() + extension;

                Path targetPath = root.resolve(fileName).normalize();

                file.transferTo(targetPath);

                return new StoredVideo(fileName);
            } catch (IOException e) {
                throw new IllegalStateException("Не удалось сохранить видео", e);
            }
        
        } catch (Exception e) {
            log.error("Error while save", e);
            throw e;
        }
    }

    @Override
    public Resource loadAsResource(String storagePath, long start, long end) {
        log.info("Starting loadAsResource");

        try {
            Path path = root.resolve(storagePath).normalize();

            try {
                return new InputStreamResource(new RangedFileInputStream(path, start, end));
            } catch (IOException e) {
                throw new IllegalStateException("Не удалось прочитать видео", e);
            }
        
        } catch (Exception e) {
            log.error("Error while loadAsResource", e);
            throw e;
        }
    }

    @Override
    public long size(String storagePath) {
        log.info("Starting size");

        try {
            Path path = root.resolve(storagePath).normalize();

            try {
                return Files.size(path);
            } catch (IOException e) {
                throw new IllegalStateException("Не удалось получить размер видео", e);
            }
        
        } catch (Exception e) {
            log.error("Error while size", e);
            throw e;
        }
    }

    private String getExtension(String originalName) {
        if (originalName == null || !originalName.contains(".")) {
            return ".mp4";
        }

        return originalName.substring(originalName.lastIndexOf("."));
    }
}