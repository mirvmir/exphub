package io.github.mirvmir.identity.application.service;

import io.github.mirvmir.common.exception.NotFoundException;
import io.github.mirvmir.identity.api.IdentityApi;
import io.github.mirvmir.identity.application.CustomUserDetails;
import io.github.mirvmir.identity.application.service.port.repository.UserRepository;
import io.github.mirvmir.identity.application.service.interfaces.AuthService;
import io.github.mirvmir.identity.domain.User;
import io.github.mirvmir.identity.dto.TokenDto;
import io.github.mirvmir.identity.dto.UserInfoDto;
import io.github.mirvmir.identity.exception.IdentityErrorCode;
import io.github.mirvmir.identity.exception.UnauthorizedException;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@AllArgsConstructor
@Service
public class DefaultIdentityApi implements IdentityApi {

    private final AuthService authService;

    private final UserRepository userRepository;

    @Override
    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new UnauthorizedException("UNAUTHORIZED", "User not authorized");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof Jwt jwt) {
            return Long.valueOf(jwt.getSubject());
        }

        if (principal instanceof CustomUserDetails userDetails) {
            return userDetails.getId();
        }

        if (principal instanceof String subject) {
            return Long.valueOf(subject);
        }

        throw new IllegalStateException(
                "Unsupported principal type: " + principal.getClass().getName()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public UserInfoDto getCurrentUserInfo() {
        Long currentUserId = getCurrentUserId();

        User user = userRepository.findById(currentUserId);

        if (user == null) {
            throw  new NotFoundException(IdentityErrorCode.USER_NOT_FOUND);
        }

        return new UserInfoDto(
                user.getId(),
                user.getEmail()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public UserInfoDto getUserInfoById(Long userId) {
        User user = userRepository.findById(userId);

        return new UserInfoDto(
                user.getId(),
                user.getEmail()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public TokenDto reissueTokens(Long userId) {
        User user = userRepository.findById(userId);

        if (user == null) {
            throw new NotFoundException(IdentityErrorCode.USER_NOT_FOUND);
        }

        return authService.generateToken(
                user.getId(),
                List.of(new SimpleGrantedAuthority(user.getRole().fromRoleToString())),
                true
        );
    }

    @Override
    public boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof Jwt)) {
            return false;
        }

        String authority = role.startsWith("ROLE_")
                ? role
                : "ROLE_" + role;

        return authentication.getAuthorities()
                .stream()
                .anyMatch(grantedAuthority ->
                        grantedAuthority.getAuthority().equals(authority));
    }
}