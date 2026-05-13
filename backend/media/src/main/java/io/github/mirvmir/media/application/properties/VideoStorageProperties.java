package io.github.mirvmir.media.application.properties;

import io.github.mirvmir.common.properties.AppProperties;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class VideoStorageProperties {

    private final AppProperties appProperties;
    private final String webRootPath;
    private final String dockerRootPath;
    private final Long maxSizeBytes;
    private final Long defaultChunkSizeBytes;

    public VideoStorageProperties(
            AppProperties appProperties,
            @Value("${media.web.storage.root_path}") String webRootPath,
            @Value("${media.docker.storage.root_path}") String dockerRootPath,
            @Value("${media.videos.storage.max_size_bytes}") Long maxSizeBytes,
            @Value("${media.videos.default.chunk_size_bytes}") Long defaultChunkSizeBytes
            ) {
        this.appProperties = appProperties;
        this.webRootPath = webRootPath;
        this.dockerRootPath = dockerRootPath;
        this.maxSizeBytes = maxSizeBytes;
        this.defaultChunkSizeBytes = defaultChunkSizeBytes;
    }

    public String getRootPath() {
        return "web".equals(appProperties.getAppMode()) ? webRootPath : dockerRootPath;
    }
}