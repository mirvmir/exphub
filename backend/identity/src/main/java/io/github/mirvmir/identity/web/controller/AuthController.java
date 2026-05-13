package io.github.mirvmir.identity.web.controller;

import io.github.mirvmir.identity.application.CustomUserDetails;
import io.github.mirvmir.identity.web.request.LoginRq;
import io.github.mirvmir.identity.web.request.RegisterRq;
import io.github.mirvmir.identity.application.service.dto.LoginDto;
import io.github.mirvmir.identity.application.service.dto.RefreshDto;
import io.github.mirvmir.identity.application.service.dto.RegisterDto;
import io.github.mirvmir.identity.dto.TokenDto;
import io.github.mirvmir.identity.application.service.interfaces.AuthService;
import io.github.mirvmir.identity.application.service.interfaces.RefreshService;
import io.github.mirvmir.identity.web.responce.AccessTokenResponse;
import io.github.mirvmir.identity.web.responce.LoginResponse;
import io.github.mirvmir.identity.web.responce.RegisterResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final RefreshService refreshService;
    private final AuthService authService;

    public AuthController(RefreshService refreshService,
                          AuthService authService) {
        this.refreshService = refreshService;
        this.authService = authService;
    }

    @GetMapping("/ping")
    public String ping() {
        return "auth ok";
    }

    @PostMapping("/login")
    public LoginResponse login(
            @RequestBody
            LoginRq request,
            @CookieValue(name = "refresh_token", required = false)
            String rawToken,
            HttpServletResponse response
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean authenticated = authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
        if (authenticated) {
            authService.logoutCurrentSession(rawToken);
        }

        LoginDto output = authService.login(request);
        Authentication auth = output.authentication();
        CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();

        TokenDto tokens = authService.generateToken(
                user.getId(),
                auth.getAuthorities(),
                user.isProfileCompleted()
        );

        ResponseCookie cookie = ResponseCookie.from("refresh_token", tokens.refreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("Strict")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());

        return new LoginResponse(tokens.accessToken());
    }

    @PostMapping("/register")
    public RegisterResponse register(
            @RequestBody
            RegisterRq request,
            @CookieValue(name = "refresh_token", required = false)
            String rawToken,
            HttpServletResponse response
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean authenticated = authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
        if (authenticated) {
            authService.logoutCurrentSession(rawToken);
        }

        RegisterDto output = authService.register(request);
        Authentication auth = output.authentication();
        CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();

        TokenDto tokens = authService.generateToken(
                user.getId(),
                auth.getAuthorities(),
                user.isProfileCompleted()
        );

        ResponseCookie cookie = ResponseCookie.from("refresh_token", tokens.refreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("Strict")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());

        return new RegisterResponse(tokens.accessToken());
    }

    @PostMapping("/refresh")
    public AccessTokenResponse refresh(
            @CookieValue(name = "refresh_token", required = false)
            String rawToken,
            HttpServletResponse response
    ) {
        RefreshDto output = refreshService.execute(rawToken);

        TokenDto tokens = authService.generateToken(
                output.userId(),
                output.authorities(),
                output.isProfileCompleted()
        );

        ResponseCookie cookie = ResponseCookie.from("refresh_token", tokens.refreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("Strict")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());

        return new AccessTokenResponse(tokens.accessToken());
    }
}