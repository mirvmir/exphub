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
                request.startAt()
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
    public Set<ActivityTimeResponse> getAvailabilityTimesForAuthor(Long activityId) {
        Activity activity = getActivityForCurrentAuthor(activityId);

        if (!activity.isIndividual()) {
            throw new BusinessException(ActivityErrorCode.ONLY_FOR_INDIVIDUAL);
        }

        return activityTimeResponseMapper.toResponseSet(activity.getActivityTimes());
    }

    @Override
    @Transactional
    public void deleteAvailabilityTime(Long activityId,
                                       Long activityTimeId) {
        Activity activity = getActivityForCurrentAuthor(activityId);

        if (!activity.isIndividual()) {
            throw new BusinessException(ActivityErrorCode.ONLY_FOR_INDIVIDUAL);
        }

        activityTimeRepository.deleteByActivityIdAndId(activityId, activityTimeId);
    }

    private Activity getActivityForCurrentAuthor(Long activityId) {
        Activity activity = activityRepository.findById(activityId);

        if (activity == null) {
            throw new NotFoundException(
                    ActivityErrorCode.ACTIVITY_NOT_FOUND,
                    "Activity with id=" + activityId + " not found"
            );
        }

        Long currentUserId = identityApi.getCurrentUserId();

        if (!activity.getAuthorId().equals(currentUserId)) {
            throw new ForbiddenException(ActivityErrorCode.ACTIVITY_FORBIDDEN);
        }

        return activity;
    }
}
