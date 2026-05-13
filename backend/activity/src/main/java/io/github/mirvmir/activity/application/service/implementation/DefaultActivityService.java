package io.github.mirvmir.activity.application.service.implementation;

import io.github.mirvmir.activity.application.persistence.mapper.ActivitySlotResponseMapper;
import io.github.mirvmir.activity.application.service.port.repository.ActivitySlotRepository;
import io.github.mirvmir.activity.application.service.interfaces.IndividualActivityAvailabilityService;
import io.github.mirvmir.activity.application.persistence.mapper.ActivityResponseMapper;
import io.github.mirvmir.activity.application.service.port.repository.ActivityRepository;
import io.github.mirvmir.activity.application.service.interfaces.ActivityService;
import io.github.mirvmir.activity.domain.Activity;
import io.github.mirvmir.activity.domain.ActivitySlot;
import io.github.mirvmir.activity.exception.ActivityErrorCode;
import io.github.mirvmir.activity.web.response.ActivityDescriptionResponse;
import io.github.mirvmir.activity.web.response.GroupActivitySlotResponse;
import io.github.mirvmir.activity.web.response.IndividualActivitySlotResponse;
import io.github.mirvmir.common.exception.NotFoundException;
import io.github.mirvmir.enrollment.api.EnrollmentApi;
import io.github.mirvmir.identity.api.IdentityApi;
import io.github.mirvmir.profile.api.ProfileApi;
import io.github.mirvmir.profile.api.dto.ProfileNameDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@Slf4j
@Service
public class DefaultActivityService implements ActivityService {

    private final IdentityApi identityApi;
    private final EnrollmentApi enrollmentApi;
    private final ProfileApi profileApi;

    private final ActivityRepository activityRepository;
    private final ActivitySlotRepository activitySlotRepository;

    private final IndividualActivityAvailabilityService individualActivityAvailabilityService;

    private final ActivityResponseMapper activityResponseMapper;
    private final ActivitySlotResponseMapper activitySlotResponseMapper;

    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public ActivityDescriptionResponse getActivity(Long id) {
        log.debug("Getting activity description: activityId={}", id);

        Activity activity = activityRepository.findById(id);

        if (activity == null
                || (!activity.isActive() && !activity.isArchive())) {
            log.warn("Activity is not available for description: activityId={}", id);
            throw new NotFoundException(
                    ActivityErrorCode.ACTIVITY_NOT_FOUND,
                    "Activity with id=" + id + " not found"
            );
        }

        Long currentUserId = identityApi.getCurrentUserId();
        Long authorId = activity.getAuthorId();

        boolean isStudent = enrollmentApi.isStudentOfActivity(
                currentUserId,
                id
        );

        if (!activity.isActive() && !isStudent) {
            log.warn("Inactive activity requested by non-student: activityId={}, userId={}", id, currentUserId);
            throw new NotFoundException(
                    ActivityErrorCode.ACTIVITY_NOT_FOUND,
                    "Activity with id=" + id + " not found"
            );
        }

        ProfileNameDto author = profileApi.getProfileName(authorId);

        Set<IndividualActivitySlotResponse> availableTimes = Set.of();
        Set<GroupActivitySlotResponse> availableSlots = Set.of();

        if (activity.isActive()) {
            List<ActivitySlot> plannedSlots =
                    activitySlotRepository.findPlannedByActivityId(activity.getId());
            log.debug("Loaded planned activity slots: activityId={}, plannedSlotsCount={}",
                    activity.getId(),
                    plannedSlots.size());

            if (activity.isIndividual()) {
                availableTimes =
                        individualActivityAvailabilityService.calculateAvailableTimes(
                                activity,
                                plannedSlots
                        );
            }

            if (activity.isGroup()) {
                Set<Long> plannedSlotIds = plannedSlots.stream()
                        .map(ActivitySlot::getId)
                        .collect(Collectors.toSet());

                Map<Long, Integer> bookedSeatsBySlotId =
                        enrollmentApi.countBookedByActivitySlotIds(plannedSlotIds, Instant.now(clock));
                log.debug("Loaded booked seats for group activity: activityId={}, slotsCount={}",
                        activity.getId(),
                        bookedSeatsBySlotId.size());

                availableSlots =
                        activitySlotResponseMapper.toAvailableGroupSlotResponseSet(
                                plannedSlots,
                                bookedSeatsBySlotId,
                                activity.getMaxBookableSeats()
                        );
            }
        }

        log.debug("Activity description prepared: activityId={}, userId={}, isStudent={}",
                activity.getId(),
                currentUserId,
                isStudent);

        return activityResponseMapper.toActivityDescriptionResponse(
                activity,
                author,
                isStudent,
                availableTimes,
                availableSlots
        );
    }
}