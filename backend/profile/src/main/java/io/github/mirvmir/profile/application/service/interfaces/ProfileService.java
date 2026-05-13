package io.github.mirvmir.profile.application.service.interfaces;


import io.github.mirvmir.profile.application.service.dto.EditProfileRqDto;
import io.github.mirvmir.profile.application.service.dto.EditProfileRsDto;
import io.github.mirvmir.profile.web.response.MyProfileResponse;
import io.github.mirvmir.profile.web.response.PublicProfileResponse;

public interface ProfileService {
    EditProfileRsDto editProfile(EditProfileRqDto dto);
    MyProfileResponse getMyProfile();
    PublicProfileResponse getPublicProfile(Long id);
}
