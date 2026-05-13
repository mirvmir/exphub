package io.github.mirvmir.media.application.properties;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class MediaProperties {
    @Value("${media.playback.token.secret}")
    private String playbackTokenSecret;

    @Value("${media.playback.token.ttl_seconds}")
    private Long playbackTokenTtlSeconds;
}
