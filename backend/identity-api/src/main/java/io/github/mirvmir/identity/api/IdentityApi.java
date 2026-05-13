package io.github.mirvmir.identity.api;

import io.github.mirvmir.identity.dto.TokenDto;
import io.github.mirvmir.identity.dto.UserInfoDto;

public interface IdentityApi {
    Long getCurrentUserId();
    UserInfoDto getCurrentUserInfo();
    UserInfoDto getUserInfoById(Long userId);
    TokenDto reissueTokens(Long userId);
    boolean hasRole(String role);
}
