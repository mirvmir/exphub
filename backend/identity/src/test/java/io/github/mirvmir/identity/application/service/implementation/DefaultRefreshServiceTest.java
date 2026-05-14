package io.github.mirvmir.identity.application.service.implementation;

import io.github.mirvmir.common.exception.NotFoundException;
import io.github.mirvmir.identity.application.RefreshTokenHasher;
import io.github.mirvmir.identity.application.properties.JwtProperties;
import io.github.mirvmir.identity.application.service.dto.RefreshDto;
import io.github.mirvmir.identity.application.service.port.repository.RefreshTokenRepository;
import io.github.mirvmir.identity.application.service.port.repository.UserRepository;
import io.github.mirvmir.identity.domain.RefreshToken;
import io.github.mirvmir.identity.domain.User;
import io.github.mirvmir.identity.exception.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DefaultRefreshServiceTest {

    private RefreshTokenHasher tokenHasher;
    private RefreshTokenRepository tokenRepo;
    private UserRepository userRepo;

    private JwtProperties jwtProperties;

    private DefaultRefreshService service;

    private final Instant now = Instant.parse("2026-05-13T10:00:00Z");
    private final Clock clock = Clock.fixed(now, ZoneOffset.UTC);

    @BeforeEach
    void setUp() {
        tokenHasher = mock(RefreshTokenHasher.class);
        tokenRepo = mock(RefreshTokenRepository.class);
        userRepo = mock(UserRepository.class);
        jwtProperties = mock(JwtProperties.class);

        service = new DefaultRefreshService(
                tokenRepo,
                userRepo,
                tokenHasher,
                jwtProperties,
                clock
        );
    }

    @Test
    void execute_shouldThrowUnauthorized_whenTokenMissing() {
        assertThrows(UnauthorizedException.class,
                () -> service.execute(null));

        verifyNoInteractions(tokenHasher, tokenRepo, userRepo);
    }

    @Test
    void execute_shouldThrowUnauthorized_whenTokenNotFound() {
        when(tokenHasher.hash("raw-token")).thenReturn("hash");
        when(tokenRepo.findByTokenHash("hash")).thenReturn(null);

        assertThrows(UnauthorizedException.class,
                () -> service.execute("raw-token"));

        verify(userRepo, never()).findById(anyLong());
    }

    @Test
    void execute_shouldDeleteAndThrowUnauthorized_whenTokenExpired() {
        User tokenUser = User.createNewCustomer("hash", "user@mail.ru");
        tokenUser.assignId(1L);

        RefreshToken refreshToken =
                new RefreshToken(tokenUser, "hash", Duration.ofMillis(-1));

        when(tokenHasher.hash("raw-token")).thenReturn("hash");
        when(tokenRepo.findByTokenHash("hash")).thenReturn(refreshToken);

        assertThrows(UnauthorizedException.class,
                () -> service.execute("raw-token"));

        verify(tokenRepo).delete(refreshToken);
        verify(userRepo, never()).findById(anyLong());
    }

    @Test
    void execute_shouldThrowNotFound_whenUserNotFound() {
        User tokenUser = User.createNewCustomer("hash", "user@mail.ru");
        tokenUser.assignId(1L);

        RefreshToken refreshToken =
                new RefreshToken(tokenUser, "hash", Duration.ofDays(1));

        when(tokenHasher.hash("raw-token")).thenReturn("hash");
        when(tokenRepo.findByTokenHash("hash")).thenReturn(refreshToken);
        when(userRepo.findById(1L)).thenReturn(null);

        assertThrows(NotFoundException.class,
                () -> service.execute("raw-token"));

        verify(tokenRepo, never()).delete(refreshToken);
    }

    @Test
    void execute_shouldReturnRefreshDtoAndDeleteOldToken() {
        User tokenUser = User.createNewCustomer("hash", "user@mail.ru");
        tokenUser.assignId(1L);

        User actualUser = User.createNewCustomer("hash", "user@mail.ru");
        actualUser.assignId(1L);
        actualUser.markProfileCompleted();

        RefreshToken refreshToken =
                new RefreshToken(tokenUser, "hash", Duration.ofDays(1));

        when(tokenHasher.hash("raw-token")).thenReturn("hash");
        when(tokenRepo.findByTokenHash("hash")).thenReturn(refreshToken);
        when(userRepo.findById(1L)).thenReturn(actualUser);

        RefreshDto result = service.execute("raw-token");

        assertEquals(1L, result.userId());
        assertTrue(result.isProfileCompleted());
        assertTrue(
                result.authorities()
                        .stream()
                        .anyMatch(authority ->
                                authority.getAuthority().equals("ROLE_USER")
                        )
        );

        verify(tokenRepo).delete(refreshToken);
    }
}