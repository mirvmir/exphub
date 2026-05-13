package io.github.mirvmir.activity.application.service;

import io.github.mirvmir.activity.api.ActivityApi;
import io.github.mirvmir.activity.api.dto.ActivityPurchaseInfoResponse;
import io.github.mirvmir.activity.api.dto.ActivitySlotPurchaseInfoResponse;
import io.github.mirvmir.activity.api.dto.CreateIndividualActivitySlotRequest;
import io.github.mirvmir.activity.api.dto.CreatedActivitySlotResponse;
import io.github.mirvmir.activity.application.persistence.mapper.ActivityResponseMapper;
import io.github.mirvmir.activity.application.persistence.mapper.ActivitySlotResponseMapper;
import io.github.mirvmir.activity.application.service.port.repository.ActivityRepository;
import io.github.mirvmir.activity.application.service.port.repository.ActivitySlotRepository;
import io.github.mirvmir.activity.domain.Activity;
import io.github.mirvmir.activity.domain.ActivitySlot;
import io.github.mirvmir.activity.exception.ActivityErrorCode;
import io.github.mirvmir.common.exception.NotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@AllArgsConstructor
@Service
public class DefaultActivityApi implements ActivityApi {

    private final ActivityRepository activityRepository;
    private final ActivitySlotRepository activitySlotRepository;

    private final ActivityResponseMapper activityResponseMapper;
    private final ActivitySlotResponseMapper activitySlotResponseMapper;

    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public ActivitySlotPurchaseInfoResponse getSlotPurchaseInfo(Long activitySlotId) {
        ActivitySlot slot = activitySlotRepository.findById(activitySlotId);

        if (slot == null) {
            return null;
        }

        Activity activity = activityRepository.findById(slot.getActivityId());

        if (activity == null || !activity.isActive()) {
            return null;
        }

        return activitySlotResponseMapper.toActivitySlotPurchaseInfoResponse(
                activity,
                slot
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ActivityPurchaseInfoResponse getPurchaseInfo(Long activityId) {
        Activity activity = activityRepository.findById(activityId);

        if (activity == null || !activity.isActive()) {
            return null;
        }

        return activityResponseMapper.toActivityPurchaseInfoResponse(activity);
    }

    @Override
    @Transactional
    public CreatedActivitySlotResponse createIndividualSlot(CreateIndividualActivitySlotRequest request) {
        Activity activity = activityRepository.findById(request.activityId());

        if (activity == null || !activity.isActive()) {
            throw new NotFoundException(
                    ActivityErrorCode.ACTIVITY_NOT_FOUND,
                    "Activity with id=" + request.activityId() + " not found"
            );
        }

        Instant now = Instant.now(clock);

        ActivitySlot slot = activity.reserveTimeByStudent(
                now,
                request.studentId(),
                request.activityTimeId(),
                request.startAt()
        );

        ActivitySlot savedSlot = activitySlotRepository.saveIndividualSlotWithAuthorLock(
                activity.getAuthorId(),
                request.activityTimeId(),
                slot
        );

        return new CreatedActivitySlotResponse(
                savedSlot.getId(),
                savedSlot.getActivityId(),
                savedSlot.getStartAt(),
                savedSlot.getEndAt()
        );
    }
}