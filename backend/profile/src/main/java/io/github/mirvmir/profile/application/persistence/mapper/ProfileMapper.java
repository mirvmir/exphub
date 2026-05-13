package io.github.mirvmir.profile.application.persistence.mapper;

import io.github.mirvmir.profile.domain.Profile;
import io.github.mirvmir.profile.application.persistence.entity.ProfileEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProfileMapper {

    ProfileEntity toEntity(Profile profile);

    default Profile toDomain(ProfileEntity entity) {
        if (entity == null) {
            return null;
        }

        return Profile.load(
                entity.getId(),
                entity.getUserId(),
                entity.getGivenName(),
                entity.getFamilyName(),
                entity.getFatherName(),
                entity.getAvatarFileId(),
                entity.isEmailVisibility()
        );
    }
}