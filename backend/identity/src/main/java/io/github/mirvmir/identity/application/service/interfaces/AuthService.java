package io.github.mirvmir.identity.application.service.interfaces;

import io.github.mirvmir.identity.web.request.LoginRq;
import io.github.mirvmir.identity.web.request.RegisterRq;
import io.github.mirvmir.identity.application.service.dto.LoginDto;
import io.github.mirvmir.identity.application.service.dto.RegisterDto;
import io.github.mirvmir.identity.dto.TokenDto;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public interface AuthService {
    LoginDto login(LoginRq request);
    void logoutCurrentSession(String rawToken);
    TokenDto generateToken(Long userId,
                           Collection<? extends GrantedAuthority> authorities,
                           boolean isProfileCompleted);
    RegisterDto register(RegisterRq request);
}