package io.github.mirvmir.activity.application.persistence.mapper;

import io.github.mirvmir.activity.api.dto.ActivityBookingInfoResponse;
import io.github.mirvmir.activity.domain.Activity;
import io.github.mirvmir.activity.web.response.*;
import io.github.mirvmir.profile.api.dto.ProfileNameDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;

@Mapper(
        componentModel = "spring",
        uses = {
                ActivityTimeResponseMapper.class,
                ActivitySlotResponseMapper.class
        }
)
public interface ActivityResponseMapper {

    @Mapping(target = "priceAmount", source = "activity.price.amount")
    @Mapping(target = "priceCurrency", source = "activity.price.currency")
    ActivityDescriptionResponse toActivityDescriptionResponse(
            Activity activity,
            ProfileNameDto author,
            boolean isStudent,
            Set<IndividualActivitySlotResponse> availableTimes,
            Set<GroupActivitySlotResponse> availableSlots
    );

    @Mapping(target = "priceAmount", source = "activity.price.amount")
    @Mapping(target = "priceCurrency", source = "activity.price.currency")
    AuthorActivityDescriptionResponse toAuthorActivityDescriptionResponse(
            Activity activity,
            ProfileNameDto author,
            boolean canEdit,
            boolean canDelete,
            boolean canRequestPublication
    );

    @Mapping(target = "priceAmount", source = "activity.price.amount")
    @Mapping(target = "priceCurrency", source = "activity.price.currency")
    ActivityResponse toActivityDto(Activity activity);

    default ActivityBookingInfoResponse toActivityPurchaseInfoResponse(Activity activity) {
        if (activity == null) {
            return null;
        }

        return new ActivityBookingInfoResponse(
                activity.getId(),
                activity.getAuthorId(),
                activity.getTitle(),
                activity.getPrice().getAmount(),
                activity.getPrice().getCurrency(),
                activity.isActive()
        );
    }
}