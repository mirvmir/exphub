package io.github.mirvmir.identity.application.service;

import io.github.mirvmir.common.exception.NotFoundException;
import io.github.mirvmir.identity.application.service.interfaces.AuthService;
import io.github.mirvmir.identity.application.service.port.repository.UserRepository;
import io.github.mirvmir.identity.domain.User;
import io.github.mirvmir.identity.dto.TokenDto;
import io.github.mirvmir.identity.dto.UserInfoDto;
import io.github.mirvmir.identity.exception.UnauthorizedException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DefaultIdentityApiTest {

    private AuthService authService;
    private UserRepository userRepository;

    private DefaultIdentityApi api;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        userRepository = mock(UserRepository.class);

        api = new DefaultIdentityApi(
                authService,
                userRepository
        );

        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }
    @Test
    void getCurrentUserId_shouldThrowException_whenNotAuthenticated() {
        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> api.getCurrentUserId()
        );

        assertEquals("UNAUTHORIZED", exception.getCode());
        assertEquals("User not authorized", exception.getMessage());
    }

    @Test
    void getCurrentUserId_shouldReturnJwtSubject() {
        SecurityContextHolder.getContext().setAuthentication(authentication());

        Long result = api.getCurrentUserId();

        assertEquals(1L, result);
    }

    @Test
    void getCurrentUserInfo_shouldReturnNull_whenNotAuthenticated() {
        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> api.getCurrentUserInfo()
        );

        assertEquals("UNAUTHORIZED", exception.getCode());
        assertEquals("User not authorized", exception.getMessage());
    }

    @Test
    void getCurrentUserInfo_shouldReturnUserInfo() {
        User user = User.createNewCustomer("hash", "user@mail.ru");
        user.assignId(1L);

        SecurityContextHolder.getContext().setAuthentication(authentication());

        when(userRepository.findById(1L)).thenReturn(user);

        UserInfoDto result = api.getCurrentUserInfo();

        assertEquals(1L, result.userId());
        assertEquals("user@mail.ru", result.email());
    }

    @Test
    void reissueTokens_shouldGenerateNewTokens() {
        User user = User.createNewCustomer("hash", "user@mail.ru");
        user.assignId(1L);
        user.markProfileCompleted();

        when(userRepository.findById(1L)).thenReturn(user);
        when(authService.generateToken(eq(1L), anyList(), eq(true)))
                .thenReturn(new TokenDto("access-token", "refresh-token"));

        TokenDto result = api.reissueTokens(1L);

        assertEquals("access-token", result.accessToken());
        assertEquals("refresh-token", result.refreshToken());
    }

    @Test
    void reissueTokens_shouldThrowNotFound_whenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(null);

        assertThrows(NotFoundException.class,
                () -> api.reissueTokens(1L));

        verifyNoInteractions(authService);
    }

    @Test
    void hasRole_shouldReturnTrue_whenUserHasRole() {
        SecurityContextHolder.getContext().setAuthentication(authentication());

        assertTrue(api.hasRole("USER"));
        assertTrue(api.hasRole("ROLE_USER"));
    }

    @Test
    void hasRole_shouldReturnFalse_whenUserDoesNotHaveRole() {
        SecurityContextHolder.getContext().setAuthentication(authentication());

        assertFalse(api.hasRole("ADMIN"));
    }

    private UsernamePasswordAuthenticationToken authentication() {
        Jwt jwt = new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "none"),
                Map.of(
                        "sub", "1",
                        "role", List.of("ROLE_USER"),
                        "profileCompleted", true
                )
        );

        return new UsernamePasswordAuthenticationToken(
                jwt,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}