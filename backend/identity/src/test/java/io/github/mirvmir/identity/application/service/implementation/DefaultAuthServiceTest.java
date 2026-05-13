package io.github.mirvmir.identity.application.service.implementation;

import io.github.mirvmir.identity.application.JwtService;
import io.github.mirvmir.identity.application.RefreshTokenHasher;
import io.github.mirvmir.identity.application.properties.JwtProperties;
import io.github.mirvmir.identity.application.service.dto.LoginDto;
import io.github.mirvmir.identity.application.service.dto.RegisterDto;
import io.github.mirvmir.identity.application.service.port.repository.RefreshTokenRepository;
import io.github.mirvmir.identity.application.service.port.repository.UserRepository;
import io.github.mirvmir.identity.domain.RefreshToken;
import io.github.mirvmir.identity.domain.Role;
import io.github.mirvmir.identity.domain.User;
import io.github.mirvmir.identity.dto.TokenDto;
import io.github.mirvmir.identity.exception.UserAlreadyExistsException;
import io.github.mirvmir.identity.web.request.LoginRq;
import io.github.mirvmir.identity.web.request.RegisterRq;
import io.github.mirvmir.profile.api.ProfileApi;
import io.github.mirvmir.profile.api.dto.CreateProfileRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DefaultAuthServiceTest {

    private AuthenticationManager authenticationManager;
    private JwtService jwtService;
    private RefreshTokenHasher refreshTokenHasher;
    private PasswordEncoder passwordEncoder;
    private UserRepository userRepo;
    private RefreshTokenRepository tokenRepo;
    private ProfileApi profileApi;
    private JwtProperties properties;

    private DefaultAuthService service;

    @BeforeEach
    void setUp() {
        authenticationManager = mock(AuthenticationManager.class);
        jwtService = mock(JwtService.class);
        refreshTokenHasher = mock(RefreshTokenHasher.class);
        passwordEncoder = mock(PasswordEncoder.class);
        userRepo = mock(UserRepository.class);
        tokenRepo = mock(RefreshTokenRepository.class);
        profileApi = mock(ProfileApi.class);
        properties = mock(JwtProperties.class);

        service = new DefaultAuthService(
                authenticationManager,
                jwtService,
                refreshTokenHasher,
                passwordEncoder,
                userRepo,
                tokenRepo,
                profileApi,
                properties
        );
    }

    @Test
    void login_shouldAuthenticateUser() {
        LoginRq request = new LoginRq("user@mail.ru", "password");

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "user@mail.ru",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                );

        when(authenticationManager.authenticate(any()))
                .thenReturn(authentication);

        LoginDto result = service.login(request);

        assertSame(authentication, result.authentication());

        verify(authenticationManager).authenticate(argThat(actual ->
                actual.getPrincipal().equals("user@mail.ru")
                && actual.getCredentials().equals("password")
        ));
    }

    @Test
    void login_shouldThrowBadCredentials_whenAuthenticationFailed() {
        LoginRq request = new LoginRq("user@mail.ru", "wrong");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("bad"));

        assertThrows(BadCredentialsException.class,
                () -> service.login(request));
    }

    @Test
    void logoutCurrentSession_shouldDoNothing_whenRawTokenIsNull() {
        service.logoutCurrentSession(null);

        verifyNoInteractions(refreshTokenHasher, tokenRepo);
    }

    @Test
    void logoutCurrentSession_shouldDoNothing_whenTokenNotFound() {
        when(refreshTokenHasher.hash("raw-token")).thenReturn("hash");
        when(tokenRepo.findByTokenHash("hash")).thenReturn(null);

        service.logoutCurrentSession("raw-token");

        verify(tokenRepo, never()).delete(any());
    }

    @Test
    void logoutCurrentSession_shouldDeleteRefreshToken() {
        RefreshToken refreshToken = mock(RefreshToken.class);

        when(refreshTokenHasher.hash("raw-token")).thenReturn("hash");
        when(tokenRepo.findByTokenHash("hash")).thenReturn(refreshToken);

        service.logoutCurrentSession("raw-token");

        verify(tokenRepo).delete(refreshToken);
    }

    @Test
    void register_shouldCreateUserProfileAndAuthentication() {
        RegisterRq request = new RegisterRq("new@mail.ru", "password");

        when(passwordEncoder.encode("password")).thenReturn("encoded-password");

        when(userRepo.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.assignId(1L);
            return user;
        });

        RegisterDto result = service.register(request);

        assertNotNull(result.authentication());
        assertEquals("new@mail.ru", result.authentication().getName());

        verify(userRepo).save(argThat(user ->
                user.getEmail().equals("new@mail.ru")
                && user.getPasswordHash().equals("encoded-password")
                && user.getRole() == Role.USER
                && !user.isProfileCompleted()
        ));

        verify(profileApi).createProfile(argThat((CreateProfileRequest actual) ->
                actual.userId().equals(1L)
        ));
    }

    @Test
    void register_shouldThrowUserAlreadyExists_whenRepositoryFailed() {
        RegisterRq request = new RegisterRq("new@mail.ru", "password");

        when(passwordEncoder.encode("password")).thenReturn("encoded-password");
        when(userRepo.save(any(User.class))).thenThrow(new RuntimeException("duplicate"));

        assertThrows(UserAlreadyExistsException.class,
                () -> service.register(request));

        verifyNoInteractions(profileApi);
    }

    @Test
    void generateToken_shouldCreateAccessAndRefreshToken() {
        User user = User.createNewCustomer("hash", "user@mail.ru");
        user.assignId(1L);

        List<SimpleGrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority("ROLE_USER"));

        when(jwtService.generateToken(1L, authorities, true))
                .thenReturn("access-token");
        when(refreshTokenHasher.hash(anyString()))
                .thenReturn("refresh-token-hash");
        when(userRepo.findById(1L)).thenReturn(user);
        when(properties.getRefreshExpiration()).thenReturn(Duration.ofDays(30));

        TokenDto result = service.generateToken(1L, authorities, true);

        assertEquals("access-token", result.accessToken());
        assertNotNull(result.refreshToken());

        verify(tokenRepo).save(argThat(refreshToken ->
                refreshToken.getUser().equals(user)
        ));
    }
}