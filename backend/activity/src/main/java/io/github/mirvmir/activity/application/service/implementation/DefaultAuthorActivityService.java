package io.github.mirvmir.activity.application.service.implementation;

import io.github.mirvmir.activity.api.event.ActivityDeleteEvent;
import io.github.mirvmir.activity.application.persistence.mapper.ActivityEventMapper;
import io.github.mirvmir.activity.application.persistence.mapper.ActivityResponseMapper;
import io.github.mirvmir.activity.application.persistence.mapper.ActivitySlotResponseMapper;
import io.github.mirvmir.activity.application.persistence.mapper.ActivityTimeResponseMapper;
import io.github.mirvmir.activity.application.service.port.event.ActivityEventPublisher;
import io.github.mirvmir.activity.application.service.port.repository.ActivityRepository;
import io.github.mirvmir.activity.application.service.port.repository.ActivitySlotRepository;
import io.github.mirvmir.activity.application.service.interfaces.AuthorActivityService;
import io.github.mirvmir.activity.domain.Activity;
import io.github.mirvmir.activity.domain.ActivitySlot;
import io.github.mirvmir.activity.domain.ActivityType;
import io.github.mirvmir.activity.exception.ActivityErrorCode;
import io.github.mirvmir.activity.web.request.CreateActivityRequest;
import io.github.mirvmir.activity.web.request.CreateGroupActivitySlotRequest;
import io.github.mirvmir.activity.web.request.UpdateActivityRequest;
import io.github.mirvmir.activity.web.response.*;
import io.github.mirvmir.common.exception.BusinessException;
import io.github.mirvmir.common.exception.ForbiddenException;
import io.github.mirvmir.common.exception.NotFoundException;
import io.github.mirvmir.common.exception.UnauthorizedException;
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
public class DefaultAuthorActivityService implements AuthorActivityService {

    private final IdentityApi identityApi;
    private final ProfileApi profileApi;
    private final EnrollmentApi enrollmentApi;

    private final ActivityRepository activityRepository;
    private final ActivitySlotRepository activitySlotRepository;

    private final ActivityResponseMapper activityResponseMapper;
    private final ActivitySlotResponseMapper activitySlotResponseMapper;
    private final ActivityEventMapper activityEventMapper;

    private final ActivityEventPublisher activityEventPublisher;

    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public List<AuthorActivityDescriptionResponse> getAllActivity() {
        log.debug("Getting all activity for author");
        Long authorId = identityApi.getCurrentUserId();

        if (authorId == null) {
            log.error("Unauthorized author request");
            throw new UnauthorizedException(
                    "UNAUTHORIZED",
                    "User not authorized"
            );
        }

        ProfileNameDto author = profileApi.getProfileName(authorId);
        List<Activity> activities = activityRepository.findByAuthorId(authorId);
        List<AuthorActivityDescriptionResponse> response = activities.stream()
                .map(activity -> activityResponseMapper.toAuthorActivityDescriptionResponse(
                        activity,
                        author,
                        activity.isEditable(),
                        !activitySlotRepository.existsPlannedByActivityId(activity.getId()),
                        activity.canRequestPublication()
                ))
                .toList();

        log.info("Author activities successfully received: authorId={}",
                authorId);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public AuthorActivityDescriptionResponse getDescription(Long id) {
        Activity activity = getActivityForCurrentAuthor(id);

        ProfileNameDto author = profileApi.getProfileName(activity.getAuthorId());

        boolean canEdit = activity.isEditable();
        boolean canDelete = !activitySlotRepository.existsPlannedByActivityId(id);
        boolean canRequestPublication = activity.canRequestPublication();

        log.info("Author activity successfully received: authorId={}, activityId={}",
                author.userId(),
                id);
        return activityResponseMapper.toAuthorActivityDescriptionResponse(
                activity,
                author,
                canEdit,
                canDelete,
                canRequestPublication
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Set<IndividualActivitySlotResponse> getIndividualSlots(Long activityId) {
        Activity activity = getActivityForCurrentAuthor(activityId);

        if (!activity.isIndividual()) {
            log.warn("Individual slots request rejected because activity is not individual: activityId={}, currentType={}",
                    activityId,
                    activity.getType());
            throw new BusinessException(ActivityErrorCode.ONLY_FOR_INDIVIDUAL);
        }

        List<ActivitySlot> slots = activitySlotRepository.findByActivityId(activityId);

        return activitySlotResponseMapper.toIndividualResponseSet(slots);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<GroupActivitySlotResponse> getGroupSlots(Long activityId) {
        Activity activity = getActivityForCurrentAuthor(activityId);

        if (!activity.isGroup()) {
            log.warn("Group slots request rejected because activity is not group: activityId={}, currentType={}",
                    activityId,
                    activity.getType());
            throw new BusinessException(ActivityErrorCode.ONLY_FOR_GROUP);
        }

        List<ActivitySlot> slots = activitySlotRepository.findByActivityId(activityId);

        Set<Long> slotIds = slots.stream()
                .map(ActivitySlot::getId)
                .collect(Collectors.toSet());

        Map<Long, Integer> bookedSeatsBySlotId =
                enrollmentApi.countBookedByActivitySlotIds(
                        slotIds,
                        Instant.now(clock)
                );

        return activitySlotResponseMapper.toGroupResponseSet(
                slots,
                bookedSeatsBySlotId,
                activity.getMaxBookableSeats()
        );
    }

    @Override
    @Transactional
    public IdResponse createActivity(CreateActivityRequest request) {
        Long currentUserId = identityApi.getCurrentUserId();

        if (currentUserId == null) {
            log.warn("Unauthorized create activity request");
            throw new UnauthorizedException("UNAUTHORIZED", "User not authorized");
        }

        log.debug("Activity creation requested: authorId={}, type={}", currentUserId, request.type());

        Activity activity = null;
        if (ActivityType.GROUP == request.type()) {
            activity = Activity.createGroup(
                    currentUserId,
                    request.title(),
                    request.shortDescription(),
                    request.descriptionHtml(),
                    request.maxBookableSeats(),
                    request.priceAmount(),
                    request.priceCurrency(),
                    request.durationMinutes()
            );
        }
        else if (ActivityType.INDIVIDUAL == request.type()) {
            activity = Activity.createIndividual(
                    currentUserId,
                    request.title(),
                    request.shortDescription(),
                    request.descriptionHtml(),
                    request.priceAmount(),
                    request.priceCurrency(),
                    request.durationMinutes()
            );
        }
        else {
            throw new IllegalStateException("Обязательно должен быть тип занятия");
        }

        activityRepository.saveOrUpdate(activity);
        log.info("Activity created: activityId={}, authorId={}, type={}",
                activity.getId(),
                currentUserId,
                request.type());

        return new IdResponse(activity.getId());
    }

    @Override
    @Transactional
    public ActivityResponse updateActivity(Long activityId,
                                           UpdateActivityRequest request) {
        log.debug("Activity update requested: activityId={}", activityId);

        Activity activity = activityRepository.findById(activityId);

        if (activity == null) {
            log.warn("Activity update failed because activity was not found: activityId={}", activityId);
            throw new NotFoundException(
                    ActivityErrorCode.ACTIVITY_NOT_FOUND,
                    "Activity with id=" + activityId + " not found"
            );
        }

        Long authorId = activity.getAuthorId();
        ensureAuthor(authorId);

        activity.edit(
                request.title(),
                request.shortDescription(),
                request.descriptionHtml(),
                request.maxBookableSeats(),
                request.priceAmount(),
                request.priceCurrency(),
                request.durationMinutes()
        );

        activityRepository.saveOrUpdate(activity);
        log.info("Activity updated: activityId={}, authorId={}", activity.getId(), authorId);

        return activityResponseMapper.toActivityDto(activity);
    }

    @Override
    @Transactional
    public void publish(Long id) {
        log.debug("Activity publication requested: activityId={}", id);

        Activity activity = activityRepository.findById(id);

        if (activity == null) {
            log.warn("Activity publication failed because activity was not found: activityId={}", id);
            throw new NotFoundException(
                    ActivityErrorCode.ACTIVITY_NOT_FOUND,
                    "Activity with id=" + id + " not found"
            );
        }

        Long authorId = activity.getAuthorId();
        ensureAuthor(authorId);

        activity.requestPublication();
        activityRepository.saveOrUpdate(activity);
        log.info("Activity publication requested: activityId={}, authorId={}", activity.getId(), authorId);
    }

    @Override
    @Transactional
    public void archive(Long id) {
        log.debug("Activity archive requested: activityId={}", id);

        Activity activity = activityRepository.findById(id);

        if (activity == null) {
            log.warn("Activity archive failed because activity was not found: activityId={}", id);
            throw new NotFoundException(
                    ActivityErrorCode.ACTIVITY_NOT_FOUND,
                    "Activity with id=" + id + " not found"
            );
        }

        Long authorId = activity.getAuthorId();
        ensureAuthor(authorId);

        activityEventPublisher.delete(
                new ActivityDeleteEvent(activity.getId())
        );

        activity.archive();
        activityRepository.saveOrUpdate(activity);
        log.info("Activity archived: activityId={}, authorId={}", activity.getId(), authorId);
    }

    @Override
    @Transactional
    public void unarchive(Long id) {
        log.debug("Activity unarchive requested: activityId={}", id);

        Activity activity = activityRepository.findById(id);

        if (activity == null) {
            log.warn("Activity unarchive failed because activity was not found: activityId={}", id);
            throw new NotFoundException(
                    ActivityErrorCode.ACTIVITY_NOT_FOUND,
                    "Activity with id=" + id + " not found"
            );
        }

        Long authorId = activity.getAuthorId();
        ensureAuthor(authorId);

        activityEventPublisher.publish(
                activityEventMapper.toPublishedEvent(activity)
        );

        activity.unarchive();
        activityRepository.saveOrUpdate(activity);
        log.info("Activity unarchived: activityId={}, authorId={}", activity.getId(), authorId);
    }

    @Override
    @Transactional
    public void deleteActivity(Long id) {
        log.debug("Activity deletion requested: activityId={}", id);

        Activity activity = activityRepository.findById(id);

        if (activity == null) {
            log.warn("Activity deletion failed because activity was not found: activityId={}", id);
            throw new NotFoundException(
                    ActivityErrorCode.ACTIVITY_NOT_FOUND,
                    "Activity with id=" + id + " not found"
            );
        }

        Long authorId = activity.getAuthorId();
        ensureAuthor(authorId);

        boolean hasPlannedSlots =
                activitySlotRepository.existsPlannedByActivityId(id);
        if (hasPlannedSlots) {
            log.warn("Activity deletion rejected because planned slots exist: activityId={}", id);
            throw new BusinessException(
                    ActivityErrorCode.ACTIVITY_HAS_PLANNED_SLOTS
            );
        }

        activity.delete();
        activityRepository.saveOrUpdate(activity);
        log.info("Activity deleted: activityId={}, authorId={}", activity.getId(), authorId);
    }

    @Override
    @Transactional
    public ActivitySlotResponse createGroupSlot(Long id,
                                                CreateGroupActivitySlotRequest request) {
        log.debug("Group activity slot creation requested: activityId={}, startTime={}", id, request.startTime());

        Activity activity = activityRepository.findById(id);

        if (activity == null || !activity.isActive()) {
            log.warn("Group activity slot creation failed because active activity was not found: activityId={}", id);
            throw new NotFoundException(
                    ActivityErrorCode.ACTIVITY_NOT_FOUND,
                    "Activity with id=" + id + " not found"
            );
        }

        Long authorId = activity.getAuthorId();

        ensureAuthor(authorId);

        Instant now = Instant.now(clock);

        ActivitySlot newSlot = activity.createSlotByTeacher(
                now,
                request.startTime()
        );
        ActivitySlot savedSlot = activitySlotRepository.saveGroupSlotWithAuthorLock(
                authorId,
                newSlot
        );

        log.info("Group activity slot created: activityId={}, activitySlotId={}, authorId={}",
                activity.getId(),
                savedSlot.getId(),
                authorId);
        return new ActivitySlotResponse(
                savedSlot.getId(),
                savedSlot.getActivityId(),
                savedSlot.getStartAt(),
                savedSlot.getEndAt()
        );
    }

    private void ensureAuthor(Long authorId) {
        Long currentUserId = identityApi.getCurrentUserId();

        if (currentUserId == null) {
            log.warn("Unauthorized author action request");
            throw new UnauthorizedException("UNAUTHORIZED", "User not authorized");
        }

        boolean isAuthor = authorId.equals(currentUserId);
        if (!isAuthor) {
            log.warn("Forbidden author activity action: expectedAuthorId={}, currentUserId={}",
                    authorId,
                    currentUserId);
            throw new ForbiddenException(ActivityErrorCode.ACTIVITY_FORBIDDEN);
        }
    }

    private Activity getActivityForCurrentAuthor(Long activityId) {
        Activity activity = activityRepository.findById(activityId);

        if (activity == null) {
            log.warn("Author activity getting failed, activity not found: activityId={}",
                    activityId);
            throw new NotFoundException(
                    ActivityErrorCode.ACTIVITY_NOT_FOUND,
                    "Activity with id=" + activityId + " not found"
            );
        }

        Long currentUserId = identityApi.getCurrentUserId();

        if (currentUserId == null) {
            log.warn("Unauthorized author request");
            throw new UnauthorizedException(
                    "UNAUTHORIZED",
                    "User not authorized"
            );
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
