package io.github.mirvmir.media.application.service.implementation;

import io.github.mirvmir.media.application.service.interfaces.MediaFileStorageService;
import io.github.mirvmir.media.domain.StoredFile;
import io.github.mirvmir.media.application.properties.MediaFileStorageProperties;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class LocalMediaFileStorageService implements MediaFileStorageService {

    private final MediaFileStorageProperties properties;

    @Override
    public StoredFile save(MultipartFile file) {
        log.info("Starting save");

        try {
            try {
                Path root = Paths.get(properties.getRootPath());
                Files.createDirectories(root);

                String extension = getExtension(file.getOriginalFilename());
                String fileName = UUID.randomUUID() + extension;

                Path targetPath = root.resolve(fileName).normalize();

                file.transferTo(targetPath);

                return new StoredFile(
                        file.getOriginalFilename(),
                        file.getContentType(),
                        file.getSize(),
                        fileName
                );
            } catch (IOException e) {
                throw new IllegalStateException("Не удалось сохранить файл", e);
            }
        
        } catch (Exception e) {
            log.error("Error while save", e);
            throw e;
        }
    }

    @Override
    public Resource loadAsResource(String storagePath) {
        log.info("Starting loadAsResource");

        try {
            Path path = Paths.get(properties.getRootPath())
                    .resolve(storagePath)
                    .normalize();

            return new FileSystemResource(path);
        
        } catch (Exception e) {
            log.error("Error while loadAsResource", e);
            throw e;
        }
    }

    private String getExtension(String originalName) {
        if (originalName == null || !originalName.contains(".")) {
            return "";
        }

        return originalName.substring(originalName.lastIndexOf("."));
    }
}