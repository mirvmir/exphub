package io.github.mirvmir.media.application.service.implementation;

import io.github.mirvmir.common.exception.UnauthorizedException;
import io.github.mirvmir.identity.api.IdentityApi;
import io.github.mirvmir.media.application.service.interfaces.MediaFileTokenService;
import io.github.mirvmir.media.application.properties.MediaProperties;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

@Service
@AllArgsConstructor
@Slf4j
public class DefaultMediaFileTokenService implements MediaFileTokenService {

    private final IdentityApi identityApi;
    private final MediaProperties mediaProperties;

    @Override
    public String createToken(Long fileId) {
        log.info("Starting createToken");

        try {
            Long userId = identityApi.getCurrentUserId();

            if (userId == null) {
                throw new UnauthorizedException("UNAUTHORIZED", "User not authorized");
            }

            long expiresAt = Instant.now()
                    .plusSeconds(mediaProperties.getPlaybackTokenTtlSeconds())
                    .getEpochSecond();

            String payload = fileId + ":" + userId + ":" + expiresAt;
            String signature = sign(payload);

            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString((payload + ":" + signature).getBytes(StandardCharsets.UTF_8));
        
        } catch (Exception e) {
            log.error("Error while createToken", e);
            throw e;
        }
    }

    @Override
    public void validateToken(Long fileId, String token) {
        log.info("Starting validateToken");

        try {
            String decoded;

            try {
                decoded = new String(
                        Base64.getUrlDecoder().decode(token),
                        StandardCharsets.UTF_8
                );
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid file token");
            }

            String[] parts = decoded.split(":");

            if (parts.length != 4) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid file token");
            }

            Long tokenFileId = Long.valueOf(parts[0]);
            long expiresAt = Long.parseLong(parts[2]);

            String payload = parts[0] + ":" + parts[1] + ":" + parts[2];
            String signature = parts[3];

            if (!fileId.equals(tokenFileId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid file token");
            }

            if (Instant.now().getEpochSecond() > expiresAt) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "File token expired");
            }

            if (!signature.equals(sign(payload))) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid file token");
            }
        
        } catch (Exception e) {
            log.error("Error while validateToken", e);
            throw e;
        }
    }

    private String sign(String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");

            SecretKeySpec key = new SecretKeySpec(
                    mediaProperties.getPlaybackTokenSecret().getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );

            mac.init(key);

            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Cannot sign file token", e);
        }
    }
}