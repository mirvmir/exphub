package io.github.mirvmir.enrollment.web.controller;

import io.github.mirvmir.enrollment.application.service.interfaces.EnrollmentBookingService;
import io.github.mirvmir.enrollment.web.request.BookIndividualActivityRequest;
import io.github.mirvmir.enrollment.web.response.BookingResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/enrollments")
public class EnrollmentController {

    private final EnrollmentBookingService enrollmentBookingService;

    @PostMapping("/courses/{courseId}/book")
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse bookCourse(
            @PathVariable("courseId")
            Long courseId
    ) {
        return enrollmentBookingService.bookCourse(
                courseId
        );
    }

    @PostMapping("/activity-slots/{activitySlotId}/book")
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse bookGroupActivitySlot(
            @PathVariable("activitySlotId")
            Long activitySlotId
    ) {
        return enrollmentBookingService.bookGroupActivitySlot(
                activitySlotId
        );
    }

    @PostMapping("/individual-activities/{activityId}/book")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<BookingResponse> bookIndividualActivity(
            @PathVariable("activityId")
            Long activityId,
            @Valid
            @RequestBody
            BookIndividualActivityRequest request
    ) {
        var response = enrollmentBookingService.bookIndividualActivity(
                activityId,
                request
        );
        return ResponseEntity.ok(response);
    }
}