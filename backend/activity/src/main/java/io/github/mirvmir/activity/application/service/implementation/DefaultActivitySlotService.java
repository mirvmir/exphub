package io.github.mirvmir.activity.application.service.implementation;

import io.github.mirvmir.activity.api.event.ActivityChangeTopicIds;
import io.github.mirvmir.activity.api.event.ActivityDeleteEvent;
import io.github.mirvmir.activity.application.service.port.event.ActivityEventPublisher;
import io.github.mirvmir.activity.application.service.port.repository.ActivityRepository;
import io.github.mirvmir.activity.application.service.port.repository.ActivitySlotRepository;
import io.github.mirvmir.activity.application.service.interfaces.ActivitySlotService;
import io.github.mirvmir.activity.domain.Activity;
import io.github.mirvmir.activity.domain.ActivitySlot;
import io.github.mirvmir.activity.exception.ActivityErrorCode;
import io.github.mirvmir.activity.application.properties.ActivityCancellationProperties;
import io.github.mirvmir.activity.web.request.CancelActivitySlotRequest;
import io.github.mirvmir.activity.web.request.UpdateActivitySlotRoomJoinUrlRequest;
import io.github.mirvmir.activity.web.request.UpdateActivityTopicsRequest;
import io.github.mirvmir.common.exception.BusinessException;
import io.github.mirvmir.common.exception.ForbiddenException;
import io.github.mirvmir.common.exception.NotFoundException;
import io.github.mirvmir.common.exception.UnauthorizedException;
import io.github.mirvmir.enrollment.api.EnrollmentApi;
import io.github.mirvmir.identity.api.IdentityApi;
import io.github.mirvmir.taxonomy.api.TaxonomyApi;
import io.github.mirvmir.taxonomy.api.dto.TopicTaxonomyInfoResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

@AllArgsConstructor
@Slf4j
@Service
public class DefaultActivitySlotService implements ActivitySlotService {

    private final IdentityApi identityApi;
    private final EnrollmentApi enrollmentApi;
    private final TaxonomyApi taxonomyApi;

    private final ActivityRepository activityRepository;
    private final ActivitySlotRepository activitySlotRepository;

    private final ActivityCancellationProperties cancellationProperties;

    private final ActivityEventPublisher activityEventPublisher;

    private final Clock clock;

    @Override
    @Transactional
    public void cancelByAuthor(Long activitySlotId,
                               CancelActivitySlotRequest request) {
        Long currentUserId = identityApi.getCurrentUserId();

        if (currentUserId == null) {
            log.info("Unauthorized author cancellation request: activitySlotId={}, reason={}",
                    activitySlotId,
                    request.reason()
            );

            throw new UnauthorizedException("UNAUTHORIZED", "User not authorized");
        }

        log.info("Author cancellation requested: activitySlotId={}, userId={}", activitySlotId, currentUserId);
        Instant now = Instant.now(clock);

        ActivitySlot slot = getExistingSlot(activitySlotId);
        Activity activity = getExistingActivity(slot.getActivityId());

        if (!activity.getAuthorId().equals(currentUserId)) {
            log.warn("Forbidden author action: activityId={}, userId={}, authorId={}",
                    activity.getId(),
                    currentUserId,
                    activity.getAuthorId()
            );
            throw new ForbiddenException(ActivityErrorCode.ACTIVITY_FORBIDDEN);
        }

        slot.cancelByAuthor(
                currentUserId,
                now,
                cancellationProperties.getMinHoursBeforeStart()
        );
        activitySlotRepository.saveOrUpdate(slot);

        enrollmentApi.cancelAllByActivitySlotId(
                slot.getId(),
                request.reason()
        );

        activityEventPublisher.delete(
                new ActivityDeleteEvent(activity.getId())
        );
        log.info("Activity slot cancelled by author: activityId={}, activitySlotId={}, userId={}",
                activity.getId(),
                slot.getId(),
                currentUserId
        );
    }

    @Override
    public void cancelByStudent(Long activitySlotId,
                                CancelActivitySlotRequest request) {
        Long currentUserId = identityApi.getCurrentUserId();

        if (currentUserId == null) {
            log.info("Unauthorized student cancellation request: activitySlotId={}, reason={}",
                    activitySlotId,
                    request.reason()
            );

            throw new UnauthorizedException("UNAUTHORIZED", "User not authorized");
        }

        log.info("Student cancellation requested: activitySlotId={}, userId={}", activitySlotId, currentUserId);
        Instant now = Instant.now(clock);

        ActivitySlot slot = getExistingSlot(activitySlotId);
        Activity activity = getExistingActivity(slot.getActivityId());

        if (activity.isIndividual()) {
            slot.cancelByStudent(
                    currentUserId,
                    now,
                    cancellationProperties.getMinHoursBeforeStart()
            );

            activitySlotRepository.saveOrUpdate(slot);

            enrollmentApi.cancelByActivitySlotIdAndStudentId(
                    slot.getId(),
                    currentUserId,
                    request.reason()
            );
        }

        if (activity.isGroup()) {
            if (!activity.isGroup()) {
                throw new BusinessException(ActivityErrorCode.ONLY_FOR_GROUP);
            }

            enrollmentApi.cancelByActivitySlotIdAndStudentId(
                    activitySlotId,
                    currentUserId,
                    request.reason()
            );
        }

        activityEventPublisher.delete(
                new ActivityDeleteEvent(activity.getId())
        );
        log.info("Activity slot cancellation by student processed: activityId={}, activitySlotId={}, userId={}",
                activity.getId(),
                slot.getId(),
                currentUserId);
    }

    @Override
    @Transactional
    public void complete(Long activitySlotId) {
        Long currentUserId = identityApi.getCurrentUserId();

        if (currentUserId == null) {
            log.info("Unauthorized activity slot completion request: activitySlotId={}",
                    activitySlotId
            );

            throw new UnauthorizedException("UNAUTHORIZED", "User not authorized");
        }

        log.info("Activity slot completion requested: activitySlotId={}, userId={}", activitySlotId, currentUserId);

        ActivitySlot slot = getExistingSlot(activitySlotId);
        Activity activity = getExistingActivity(slot.getActivityId());

        if (!activity.getAuthorId().equals(currentUserId)) {
            log.warn("Forbidden author action: activityId={}, activitySlotId={}, userId={}, authorId={}",
                    activity.getId(),
                    activitySlotId,
                    currentUserId,
                    activity.getAuthorId());
            throw new ForbiddenException(ActivityErrorCode.ACTIVITY_FORBIDDEN);
        }

        slot.complete();

        activitySlotRepository.saveOrUpdate(slot);
        log.info("Activity slot completed: activityId={}, activitySlotId={}, userId={}",
                activity.getId(),
                slot.getId(),
                currentUserId);
    }

    @Override
    @Transactional
    public void updateRoomJoinUrl(Long activitySlotId,
                                  UpdateActivitySlotRoomJoinUrlRequest request) {
        Long currentUserId = identityApi.getCurrentUserId();

        if (currentUserId == null) {
            log.info("Unauthorized room join URL update request: activitySlotId={}",
                    activitySlotId
            );

            throw new UnauthorizedException("UNAUTHORIZED", "User not authorized");
        }

        Instant now = Instant.now(clock);
        log.info("Room join URL update requested: activitySlotId={}, userId={}", activitySlotId, currentUserId);

        ActivitySlot slot = getExistingSlot(activitySlotId);
        Activity activity = getExistingActivity(slot.getActivityId());

        if (!activity.getAuthorId().equals(currentUserId)) {
            log.warn("Forbidden author action: activityId={}, activitySlotId={}, userId={}, authorId={}",
                    activity.getId(),
                    activitySlotId,
                    currentUserId,
                    activity.getAuthorId());
            throw new ForbiddenException(ActivityErrorCode.ACTIVITY_FORBIDDEN);
        }

        slot.updateRoomJoinUrl(
                request.roomJoinUrl(),
                now
        );

        activitySlotRepository.saveOrUpdate(slot);
        log.info("Room join URL updated: activityId={}, activitySlotId={}, userId={}",
                activity.getId(),
                slot.getId(),
                currentUserId);
    }

    @Override
    public void updateTopics(Long activityId, UpdateActivityTopicsRequest request) {
        Long currentUserId = identityApi.getCurrentUserId();

        if (currentUserId == null) {
            log.info("Unauthorized topics update request: activityId={}",
                    activityId
            );

            throw new UnauthorizedException("UNAUTHORIZED", "User not authorized");
        }

        log.info("Activity topics update requested: activityId={}, userId={}", activityId, currentUserId);

        Activity activity = getExistingActivity(activityId);

        if (!activity.getAuthorId().equals(currentUserId)) {
            log.warn("Forbidden author action: activityId={}, userId={}, authorId={}",
                    activity.getId(),
                    currentUserId,
                    activity.getAuthorId());
            throw new ForbiddenException(ActivityErrorCode.ACTIVITY_FORBIDDEN);
        }

        List<TopicTaxonomyInfoResponse> topicsInfo = taxonomyApi.getTopicTaxonomyInfo(request.topicIds());
        boolean inSubject = topicsInfo.stream()
                .allMatch(topicInfo -> request.subjectId().equals(topicInfo.subjectId()));
        if (!inSubject) {
            throw new BusinessException(ActivityErrorCode.TOPIC_SUBJECT_MISMATCH);
        }

        activity.updateTopics(request.topicIds(), request.subjectId());

        Activity savedActivity = activityRepository.saveOrUpdate(activity);
        activityEventPublisher.changeTopic(
                new ActivityChangeTopicIds(
                        savedActivity.getId(),
                        savedActivity.getTopicIds()
                )
        );
        log.info("Activity topics updated: activityId={}, userId={}, topicsCount={}",
                savedActivity.getId(),
                currentUserId,
                savedActivity.getTopicIds() == null ? 0 : savedActivity.getTopicIds().size());
    }

    private ActivitySlot getExistingSlot(Long activitySlotId) {
        ActivitySlot slot = activitySlotRepository.findById(activitySlotId);

        if (slot == null) {
            log.warn("Activity slot not found: activitySlotId={}", activitySlotId);
            throw new NotFoundException(
                    ActivityErrorCode.ACTIVITY_SLOT_NOT_FOUND,
                    "Activity slot with id=" + activitySlotId + " not found"
            );
        }

        return slot;
    }

    private Activity getExistingActivity(Long activityId) {
        Activity activity = activityRepository.findById(activityId);

        if (activity == null) {
            log.warn("Activity not found: activityId={}", activityId);
            throw new NotFoundException(
                    ActivityErrorCode.ACTIVITY_NOT_FOUND,
                    "Activity with id=" + activityId + " not found"
            );
        }

        return activity;
    }
}