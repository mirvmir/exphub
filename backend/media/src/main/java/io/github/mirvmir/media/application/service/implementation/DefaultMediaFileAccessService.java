package io.github.mirvmir.media.application.service.implementation;

import io.github.mirvmir.common.exception.UnauthorizedException;
import io.github.mirvmir.course.api.CourseApi;
import io.github.mirvmir.enrollment.api.EnrollmentApi;
import io.github.mirvmir.identity.api.IdentityApi;
import io.github.mirvmir.media.application.service.interfaces.MediaFileAccessService;
import io.github.mirvmir.profile.api.ProfileApi;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@AllArgsConstructor
@Service
@Slf4j
public class DefaultMediaFileAccessService implements MediaFileAccessService {

    private final IdentityApi identityApi;
    private final EnrollmentApi enrollmentApi;
    private final ProfileApi profileApi;
    private final CourseApi courseApi;

    @Override
    public void checkCurrentUserCanAccess(Long fileId) {
        log.info("Starting checkCurrentUserCanAccess");

        try {
            Long userId = identityApi.getCurrentUserId();

            if (userId == null) {
                throw new UnauthorizedException("UNAUTHORIZED", "User not authorized");
            }

            if (identityApi.hasRole("ADMIN")) {
                return;
            }

            boolean allowed = courseApi.canTeacherAccessFile(userId, fileId)
                    || enrollmentApi.canUserAccessFile(userId, fileId);

            if (!allowed) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
        
        } catch (Exception e) {
            log.error("Error while checkCurrentUserCanAccess", e);
            throw e;
        }
    }

    @Override
    public void checkCanBePublic(Long fileId) {
        log.info("Starting checkCanBePublic");

        try {
            boolean exists = profileApi.existsPublicAvatar(fileId);

            if (!exists) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
        
        } catch (Exception e) {
            log.error("Error while checkCanBePublic", e);
            throw e;
        }
    }
}
