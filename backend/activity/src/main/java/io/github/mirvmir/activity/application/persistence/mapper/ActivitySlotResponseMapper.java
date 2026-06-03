package io.github.mirvmir.activity.application.persistence.mapper;

import io.github.mirvmir.activity.api.dto.ActivitySlotBookingInfoResponse;
import io.github.mirvmir.activity.domain.Activity;
import io.github.mirvmir.activity.domain.ActivitySlot;
import io.github.mirvmir.activity.web.response.GroupActivitySlotResponse;
import io.github.mirvmir.activity.web.response.IndividualActivitySlotResponse;
import org.mapstruct.Mapper;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ActivitySlotResponseMapper {

    default Set<GroupActivitySlotResponse> toAvailableGroupSlotResponseSet(
            List<ActivitySlot> plannedSlots,
            Map<Long, Integer> bookedSeatsBySlotId,
            Integer maxSeats
    ) {
        if (plannedSlots == null) {
            return new LinkedHashSet<>();
        }

        return plannedSlots.stream()
                .map(slot -> toGroupActivitySlotResponse(
                        slot,
                        bookedSeatsBySlotId.getOrDefault(slot.getId(), 0),
                        maxSeats
                ))
                .filter(slot -> slot.bookedSeats() < slot.maxSeats())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    default GroupActivitySlotResponse toGroupActivitySlotResponse(
            ActivitySlot slot,
            Integer bookedSeats,
            Integer maxSeats
    ) {
        return new GroupActivitySlotResponse(
                slot.getId(),
                slot.getActivityId(),
                slot.getStartAt(),
                slot.getEndAt(),
                bookedSeats,
                maxSeats
        );
    }

    default Set<IndividualActivitySlotResponse> toIndividualResponseSet(
            List<ActivitySlot> plannedSlots
    ) {
        if (plannedSlots == null) {
            return new LinkedHashSet<>();
        }

        return plannedSlots.stream()
                .map(this::toIndividualResponse)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    default IndividualActivitySlotResponse toIndividualResponse(ActivitySlot slot) {
        return new IndividualActivitySlotResponse(
                slot.getId(),
                slot.getActivityId(),
                slot.getStartAt(),
                slot.getEndAt()
        );
    }

    default Set<GroupActivitySlotResponse> toGroupResponseSet(
            List<ActivitySlot> plannedSlots,
            Map<Long, Integer> bookedSeatsBySlotId,
            Integer maxSeats
    ) {
        if (plannedSlots == null) {
            return new LinkedHashSet<>();
        }

        return plannedSlots.stream()
                .map(slot -> toGroupResponse(
                        slot,
                        bookedSeatsBySlotId.getOrDefault(slot.getId(), 0),
                        maxSeats
                ))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    default GroupActivitySlotResponse toGroupResponse(
            ActivitySlot slot,
            Integer bookedSeats,
            Integer maxSeats
    ) {
        return new GroupActivitySlotResponse(
                slot.getId(),
                slot.getActivityId(),
                slot.getStartAt(),
                slot.getEndAt(),
                bookedSeats,
                maxSeats
        );
    }

    default ActivitySlotBookingInfoResponse toActivitySlotPurchaseInfoResponse(
            Activity activity,
            ActivitySlot slot
    ) {
        if (activity == null || slot == null) {
            return null;
        }

        return new ActivitySlotBookingInfoResponse(
                slot.getId(),
                activity.getId(),
                activity.getAuthorId(),
                activity.getTitle(),
                slot.getStartAt(),
                slot.getEndAt(),
                activity.getMaxBookableSeats(),
                activity.getPrice().getAmount(),
                activity.getPrice().getCurrency(),
                activity.isActive()
        );
    }
}