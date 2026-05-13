package io.github.mirvmir.profile.application.service.mapper;

import io.github.mirvmir.profile.api.dto.ProfileNameDto;
import io.github.mirvmir.profile.domain.Profile;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProfileNameMapper {

    ProfileNameDto toDto(Profile profile);
}
