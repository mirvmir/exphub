package io.github.mirvmir.activity.application.service.implementation;

import io.github.mirvmir.activity.api.event.ActivityDeleteEvent;
import io.github.mirvmir.activity.application.service.port.event.ActivityEventPublisher;
import io.github.mirvmir.activity.application.service.port.repository.ActivityRepository;
import io.github.mirvmir.activity.application.service.port.repository.ActivitySlotRepository;
import io.github.mirvmir.activity.application.persistence.mapper.ActivityEventMapper;
import io.github.mirvmir.activity.application.service.interfaces.ModerationActivityService;
import io.github.mirvmir.activity.domain.Activity;
import io.github.mirvmir.activity.domain.ActivitySlot;
import io.github.mirvmir.activity.exception.ActivityErrorCode;
import io.github.mirvmir.activity.web.request.RejectActivityRequest;
import io.github.mirvmir.common.exception.NotFoundException;
import io.github.mirvmir.enrollment.api.EnrollmentApi;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@AllArgsConstructor
@Slf4j
@Service
public class DefaultModerationActivityService implements ModerationActivityService {

    private final EnrollmentApi enrollmentApi;
    private final ActivityRepository activityRepository;
    private final ActivitySlotRepository activitySlotRepository;
    private final ActivityEventPublisher activityEventPublisher;
    private final ActivityEventMapper activityEventMapper;

    @Override
    @Transactional
    public void approve(Long id) {
        log.debug("Activity moderation approval requested: activityId={}", id);

        Activity activity = activityRepository.findById(id);

        if (activity == null) {
            log.warn("Activity not found for moderation action: activityId={}", id);
            throw new NotFoundException(
                    ActivityErrorCode.ACTIVITY_NOT_FOUND,
                    "Activity with id=" + id + " not found"
            );
        }

        activity.approveModeration();
        activityRepository.saveOrUpdate(activity);

        activityEventPublisher.publish(
                activityEventMapper.toPublishedEvent(activity)
        );
        log.info("Activity approved by moderation: activityId={}", activity.getId());
    }

    @Override
    @Transactional
    public void reject(Long id,
                       RejectActivityRequest request) {
        log.debug("Activity moderation rejection requested: activityId={}", id);

        Activity activity = activityRepository.findById(id);

        if (activity == null) {
            log.warn("Activity not found: activityId={}", id);
            throw new NotFoundException(
                    ActivityErrorCode.ACTIVITY_NOT_FOUND,
                    "Activity with id=" + id + " not found"
            );
        }

        activity.rejectModeration(request.moderationComment());
        activityRepository.saveOrUpdate(activity);
        log.info("Activity rejected by moderation: activityId={}", activity.getId());
    }

    @Override
    @Transactional
    public void block(Long id) {
        log.debug("Activity blocking requested: activityId={}", id);

        Activity activity = activityRepository.findById(id);

        if (activity == null) {
            log.warn("Activity not found: activityId={}", id);
            throw new NotFoundException(
                    ActivityErrorCode.ACTIVITY_NOT_FOUND,
                    "Activity with id=" + id + " not found"
            );
        }

        activity.block();
        activityRepository.saveOrUpdate(activity);

        activityRepository.findById(activity.getId());
        List<ActivitySlot> activitySlots = activitySlotRepository.findByActivityId(activity.getId());
        log.debug("Refunding enrollments after activity block: activityId={}, slotsCount={}",
                activity.getId(),
                activitySlots.size());

        for (ActivitySlot activitySlot : activitySlots) {
            enrollmentApi.refundPayedActivityEnrollments(
                    activitySlot.getId(),
                    "Занятие заблокировано"
            );
        }

        activityEventPublisher.delete(
                new ActivityDeleteEvent(activity.getId())
        );
        log.info("Activity blocked: activityId={}", activity.getId());
    }
}
