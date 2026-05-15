package io.github.mirvmir.media.application.service.implementation;

import io.github.mirvmir.common.exception.UnauthorizedException;
import io.github.mirvmir.course.api.CourseApi;
import io.github.mirvmir.enrollment.api.EnrollmentApi;
import io.github.mirvmir.identity.api.IdentityApi;
import io.github.mirvmir.media.application.service.interfaces.VideoAccessService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@AllArgsConstructor
@Slf4j
public class DefaultVideoAccessService implements VideoAccessService {

    private final IdentityApi identityApi;
    private final CourseApi courseApi;
    private final EnrollmentApi enrollmentApi;

    @Override
    public void checkCurrentUserCanWatch(Long videoId) {
        log.info("Starting checkCurrentUserCanWatch");

        try {
            Long userId = identityApi.getCurrentUserId();

            if (userId == null) {
                throw new UnauthorizedException("UNAUTHORIZED", "User not authorized");
            }

            if (identityApi.hasRole("ADMIN")) {
                return;
            }

            boolean allowed = courseApi.canTeacherAccessVideo(userId, videoId)
                    || enrollmentApi.canUserAccessVideo(userId, videoId);

            if (!allowed) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
        
        } catch (Exception e) {
            log.error("Error while checkCurrentUserCanWatch", e);
            throw e;
        }
    }
}