package io.github.mirvmir.identity.application.service.implementation;

import io.github.mirvmir.common.exception.NotFoundException;
import io.github.mirvmir.identity.application.RefreshTokenHasher;
import io.github.mirvmir.identity.application.properties.JwtProperties;
import io.github.mirvmir.identity.application.service.dto.RefreshDto;
import io.github.mirvmir.identity.application.service.port.repository.RefreshTokenRepository;
import io.github.mirvmir.identity.application.service.port.repository.UserRepository;
import io.github.mirvmir.identity.application.service.interfaces.RefreshService;
import io.github.mirvmir.identity.domain.RefreshToken;
import io.github.mirvmir.identity.domain.Role;
import io.github.mirvmir.identity.domain.User;
import io.github.mirvmir.identity.exception.IdentityErrorCode;
import io.github.mirvmir.common.exception.UnauthorizedException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Slf4j
@AllArgsConstructor
@Service
public class DefaultRefreshService implements RefreshService {

    private final RefreshTokenRepository tokenRepo;
    private final UserRepository userRepo;

    private final RefreshTokenHasher tokenHasher;
    private final JwtProperties jwtProperties;
    private final Clock clock;

    @Transactional
    @Override
    public RefreshDto execute(String rawToken) {
        log.info("Refresh token execution started");

        if (rawToken == null) {
            log.error("Refresh token execution failed: refreshToken is null");
            throw new UnauthorizedException("UNAUTHORIZED",
                    "Refresh token missing");
        }

        String hashToken = tokenHasher.hash(rawToken);
        RefreshToken refreshToken = tokenRepo.findByTokenHash(hashToken);

        if (refreshToken == null) {
            log.error("Refresh token execution failed: refreshToken not found");
            throw new UnauthorizedException("UNAUTHORIZED",
                    "Invalid refresh token");
        }

        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            log.error("Refresh token execution failed: refreshToken expired, userId={}",
                    refreshToken.getUser().getId());
            tokenRepo.delete(refreshToken);
            throw new UnauthorizedException("UNAUTHORIZED",
                    "Refresh token expired");
        }

        User user = userRepo.findById(refreshToken.getUser().getId());
        if (user == null) {
            log.error("Refresh token execution failed: user not found, userId={}",
                    refreshToken.getUser().getId());
            throw new NotFoundException(IdentityErrorCode.USER_NOT_FOUND);
        }

        tokenRepo.delete(refreshToken);

        Role role = user.getRole();
        log.info("Refresh token execution completed successfully: userId={}", user.getId());

        return new RefreshDto(
                user.getId(),
                List.of(new SimpleGrantedAuthority("ROLE_" + role.name())),
                user.isProfileCompleted()
        );
    }

    @Override
    public void deleteExpiredRefreshTokens() {
        Instant now = Instant.now(clock);

        log.info("Start expiring overdue orders: now={}",
                now);

        for (int i = 0; i < jwtProperties.getMaxBatch(); i++) {
            int deleted = tokenRepo.deleteExpiredBatch(now, jwtProperties.getBatch());

            if (0 == deleted) {
                break;
            }
        }
    }
}

