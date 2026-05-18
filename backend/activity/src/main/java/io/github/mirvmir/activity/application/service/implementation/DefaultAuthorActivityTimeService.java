package io.github.mirvmir.activity.application.service.implementation;

import io.github.mirvmir.activity.application.persistence.mapper.ActivityTimeResponseMapper;
import io.github.mirvmir.activity.application.service.port.repository.ActivityRepository;
import io.github.mirvmir.activity.application.service.port.repository.ActivityTimeRepository;
import io.github.mirvmir.activity.web.response.ActivityTimeResponse;
import io.github.mirvmir.activity.application.service.interfaces.AuthorActivityTimeService;
import io.github.mirvmir.activity.domain.Activity;
import io.github.mirvmir.activity.domain.ActivityTime;
import io.github.mirvmir.activity.exception.ActivityErrorCode;
import io.github.mirvmir.activity.web.request.CreateAvailabilityTimeRequest;
import io.github.mirvmir.common.exception.BusinessException;
import io.github.mirvmir.common.exception.ForbiddenException;
import io.github.mirvmir.common.exception.NotFoundException;
import io.github.mirvmir.common.exception.UnauthorizedException;
import io.github.mirvmir.identity.api.IdentityApi;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Set;

@Slf4j
@Service
@AllArgsConstructor
public class DefaultAuthorActivityTimeService implements AuthorActivityTimeService {

    private final IdentityApi identityApi;

    private final ActivityRepository activityRepository;
    private final ActivityTimeRepository activityTimeRepository;

    private final ActivityTimeResponseMapper activityTimeResponseMapper;

    private final Clock clock;

    @Override
    @Transactional
    public ActivityTimeResponse createAvailabilityTime(Long activityId,
                                                       CreateAvailabilityTimeRequest request) {
        log.debug("Availability time creation requested: activityId={}, startAt={}",
                activityId,
                request.startAt());

        Activity activity = activityRepository.findById(activityId);

        if (activity == null) {
            log.warn("Activity not found for availability time creation: activityId={}", activityId);
            throw new NotFoundException(
                    ActivityErrorCode.ACTIVITY_NOT_FOUND,
                    "Activity with id=" + activityId + " not found"
            );
        }

        Long currentUserId = identityApi.getCurrentUserId();

        if (currentUserId == null) {
            log.warn("Unauthorized create availability time request");
            throw new UnauthorizedException("UNAUTHORIZED", "User not authorized");
        }

        if (!activity.getAuthorId().equals(currentUserId)) {
            log.warn("Forbidden availability time creation: activityId={}, userId={}, authorId={}",
                    activityId,
                    currentUserId,
                    activity.getAuthorId());
            throw new ForbiddenException(
                    ActivityErrorCode.ACTIVITY_FORBIDDEN
            );
        }

        Instant now = Instant.now(clock);
        ActivityTime activityTime = activity.createAvailabilityTime(
                now,
                request.startAt(),
                request.endAt()
        );

        ActivityTime savedActivityTime = activityTimeRepository.save(
                activityId,
                activityTime
        );

        activityRepository.saveOrUpdate(activity);
        log.info("Availability time created: activityId={}, activityTimeId={}, authorId={}",
                activity.getId(),
                activityTime.getId(),
                currentUserId);

        return activityTimeResponseMapper.toResponse(activityTime);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<ActivityTimeResponse> getAvailabilityTimes(Long activityId) {
        log.debug("Availability times requested: activityId={}",
                activityId);
        Activity activity = getActivityForCurrentAuthor(activityId);

        if (!activity.isIndividual()) {
            log.warn("Availability times request rejected because activity is not individual: activityId={}, currentType={}",
                    activityId,
                    activity.getType());
            throw new BusinessException(ActivityErrorCode.ONLY_FOR_INDIVIDUAL);
        }

        log.info("Availability times successfully received: activityId={}, count={}",
                activityId,
                activity.getActivityTimes() == null
                        ? 0
                        : activity.getActivityTimes().size());
        return activityTimeResponseMapper.toResponseSet(activity.getActivityTimes());
    }

    @Override
    @Transactional
    public void deleteAvailabilityTime(Long activityId,
                                       Long activityTimeId) {
        log.debug("Availability time deletion requested: activityId={}, activityTimeId={}",
                activityId,
                activityTimeId);
        Activity activity = getActivityForCurrentAuthor(activityId);

        if (!activity.isIndividual()) {
            log.warn("Availability time deletion rejected because activity is not individual: activityId={}, activityTimeId={}, currentType={}",
                    activityId,
                    activityTimeId,
                    activity.getType());
            throw new BusinessException(ActivityErrorCode.ONLY_FOR_INDIVIDUAL);
        }

        activityTimeRepository.deleteByActivityIdAndId(activityId, activityTimeId);
        log.info("Availability time deleted: activityId={}, activityTimeId={}",
                activityId,
                activityTimeId);
    }

    private Activity getActivityForCurrentAuthor(Long activityId) {
        Activity activity = activityRepository.findById(activityId);

        if (activity == null) {
            log.warn("Author activity lookup failed because activity was not found: activityId={}", activityId);
            throw new NotFoundException(
                    ActivityErrorCode.ACTIVITY_NOT_FOUND,
                    "Activity with id=" + activityId + " not found"
            );
        }

        Long currentUserId = identityApi.getCurrentUserId();

        if (currentUserId == null) {
            log.warn("Unauthorized get activity for author");
            throw new UnauthorizedException("UNAUTHORIZED", "User not authorized");
        }

        if (!activity.getAuthorId().equals(currentUserId)) {
            log.warn("Forbidden author activity access: activityId={}, expectedAuthorId={}, currentUserId={}",
                    activityId,
                    activity.getAuthorId(),
                    currentUserId);
            throw new ForbiddenException(ActivityErrorCode.ACTIVITY_FORBIDDEN);
        }

        return activity;
    }
}
