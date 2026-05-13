package io.github.mirvmir.profile.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import static lombok.AccessLevel.PRIVATE;

@Getter
@AllArgsConstructor(access = PRIVATE)
public class Profile {
    private Long id;
    @NonNull
    private Long userId;
    private String givenName;
    private String familyName;
    private String fatherName;
    private Long avatarFileId;
    @NonNull
    private boolean emailVisibility;

    public static Profile createNew(Long userId) {
        return new Profile(
                null,
                userId,
                null,
                null,
                null,
                null,
                false
        );
    }

    public static Profile load(Long id,
                               Long userId,
                               String givenName,
                               String familyName,
                               String fatherName,
                               Long avatarFileId,
                               boolean emailVisibility) {
        return new Profile(
                id,
                userId,
                givenName,
                familyName,
                fatherName,
                avatarFileId,
                emailVisibility
        );
    }

    public void change(String givenName,
                       String familyName,
                       String fatherName,
                       Long avatarFileId,
                       boolean emailVisibility) {
        this.givenName = givenName;
        this.familyName = familyName;
        this.fatherName = fatherName;
        this.avatarFileId = avatarFileId;
        this.emailVisibility = emailVisibility;
    }

    public boolean isCompleted() {
        return !(this.givenName == null || this.givenName.isBlank())
                && !(this.familyName == null || this.familyName.isBlank());
    }

    public void assignId(Long id) {
        this.id = id;
    }
}
