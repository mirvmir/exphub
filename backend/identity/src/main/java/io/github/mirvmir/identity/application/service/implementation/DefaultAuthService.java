package io.github.mirvmir.identity.application.service.implementation;

import io.github.mirvmir.identity.application.CustomUserDetails;
import io.github.mirvmir.identity.application.JwtService;
import io.github.mirvmir.identity.application.RefreshTokenHasher;
import io.github.mirvmir.identity.application.properties.JwtProperties;
import io.github.mirvmir.identity.web.request.LoginRq;
import io.github.mirvmir.identity.web.request.RegisterRq;
import io.github.mirvmir.identity.application.service.dto.LoginDto;
import io.github.mirvmir.identity.application.service.dto.RegisterDto;
import io.github.mirvmir.identity.dto.TokenDto;
import io.github.mirvmir.identity.application.service.port.repository.RefreshTokenRepository;
import io.github.mirvmir.identity.application.service.port.repository.UserRepository;
import io.github.mirvmir.identity.application.service.interfaces.AuthService;
import io.github.mirvmir.identity.domain.RefreshToken;
import io.github.mirvmir.identity.domain.User;
import io.github.mirvmir.identity.exception.UserAlreadyExistsException;
import io.github.mirvmir.profile.api.ProfileApi;
import io.github.mirvmir.profile.api.dto.CreateProfileRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.UUID;

@Slf4j
@AllArgsConstructor
@Service
public class DefaultAuthService implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenHasher refreshTokenHasher;
    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepo;
    private final RefreshTokenRepository tokenRepo;

    private final ProfileApi profileApi;

    private final JwtProperties properties;

    @Override
    @Transactional
    public LoginDto login(LoginRq request) {
        log.info("User login started: email={}", request.email());

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email(),
                            request.password()
                    )
            );
        } catch (AuthenticationException e) {
            log.error("User login failed: email={}", request.email(), e);
            throw new BadCredentialsException("Invalid email or password");
        }

        log.info("User login completed successfully: email={}", request.email());
        return new LoginDto(authentication);
    }

    @Override
    @Transactional
    public void logoutCurrentSession(String rawToken) {
        log.info("User logout started");

        if (rawToken == null) {
            log.info("User logout skipped: refreshToken is null");
            return;
        }

        String hashToken = refreshTokenHasher.hash(rawToken);
        RefreshToken refreshToken = tokenRepo.findByTokenHash(hashToken);

        if (refreshToken == null) {
            log.info("User logout skipped: refreshToken not found");
            return;
        }

        tokenRepo.delete(refreshToken);
        log.info("User logout completed successfully: userId={}", refreshToken.getUser().getId());
    }

    @Override
    @Transactional
    public RegisterDto register(RegisterRq request) {
        log.info("User registration started: email={}", request.email());

        User newCustomer = User.createNewCustomer(
                passwordEncoder.encode(request.password()),
                request.email()
        );
        User savedUser = null;
        try {
            savedUser = userRepo.save(newCustomer);
        } catch (Exception e) {
            log.error("User registration failed: email={}", request.email(), e);
            throw new UserAlreadyExistsException("USER_ALREADY_EXISTS",
                    "User with this email already exists");
        }

        profileApi.createProfile(
                new CreateProfileRequest(
                        savedUser.getId()
                )
        );

        UserDetails userDetails = new CustomUserDetails(newCustomer);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );

        log.info("User registration completed successfully: userId={}, email={}",
                savedUser.getId(),
                savedUser.getEmail());

        return new RegisterDto(authentication);
    }

    @Override
    @Transactional
    public TokenDto generateToken(Long userId,
                                  Collection<? extends GrantedAuthority> authorities,
                                  boolean isProfileCompleted) {
        log.info("Token generation started: userId={}", userId);

        String accessToken = jwtService.generateToken(userId, authorities, isProfileCompleted);

        String rawToken = UUID.randomUUID().toString();
        String tokenHash = refreshTokenHasher.hash(rawToken);

        RefreshToken refreshToken = new RefreshToken(
                userRepo.findById(userId),
                tokenHash,
                properties.getRefreshExpiration()
        );
        tokenRepo.save(refreshToken);
        // Подумать над ошибками возможными!!!!!! Потому что они если вылезут, то уже после flush...
        // чисто в теории может быть одинаковый хэш...

        log.info("Token generation completed successfully: userId={}", userId);
        return new TokenDto(accessToken, rawToken);
    }
}
