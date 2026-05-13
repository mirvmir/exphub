package io.github.mirvmir.enrollment.exception;

import io.github.mirvmir.common.exception.ErrorCode;

public enum EnrollmentErrorCode implements ErrorCode {
    BOOKING_EXPIRED("BOOKING_EXPIRED",
            "Booking has expired"),
    BOOKING_NOT_EXPIRED("BOOKING_NOT_EXPIRED",
            "Booking has not expired yet"),
    ENROLLMENT_NOT_BOOKED("ENROLLMENT_NOT_BOOKED",
            "Enrollment is not booked"),
    ENROLLMENT_NOT_PAYED("ENROLLMENT_NOT_PAYED",
            "Enrollment is not payed"),
    ENROLLMENT_ALREADY_PAYED("ENROLLMENT_ALREADY_PAYED",
            "Course is payed"),
    COURSE_NOT_BOOKED("COURSE_NOT_BOOKED",
            "Course is not booked"),
    ORDER_ALREADY_PROCESSED("ORDER_ALREADY_PROCESSED",
            "Order already processed"),
    ACTIVITY_NOT_FOUND("ACTIVITY_NOT_FOUND",
            "Activity not found"),
    ACTIVITY_ALREADY_BOOKED("ACTIVITY_ALREADY_BOOKED",
            "Activity is already booked, please check the enrollment"),
    ACTIVITY_SLOT_FULL("ACTIVITY_SLOT_FULL",
            "Activity slot full"),
    ACTIVITY_SLOT_NOT_FOUND("ACTIVITY_SLOT_NOT_FOUND",
            "Activity slot not found"),
    COURSE_NOT_FOUND("COURSE_NOT_FOUND",
            "Course not found"),
    COURSE_ALREADY_PAYED("COURSE_ALREADY_PAYED",
            "Course already payed"),
    ORDER_NOT_FOUND("ORDER_NOT_FOUND",
            "Order not found"),
    ENROLLMENT_NOT_FOUND("ENROLLMENT_NOT_FOUND",
            "Enrollment not found"),
    COURSE_NOT_PAYED("COURSE_NOT_PAYED",
            "Course not payed"),
    COURSE_LESSON_NOT_OPENED("COURSE_LESSON_NOT_OPENED",
            "Course lesson not opened"),
    PRACTICE_NOT_COMPLETED("PRACTICE_NOT_COMPLETED",
            "Practice not completed"),
    ORDER_NOT_PAYED("ORDER_NOT_PAYED",
            "Order not payed"),
    ORDER_CANNOT_BE_REFUNDED("ORDER_CANNOT_BE_REFUNDED",
            "ORDER_CANNOT_BE_REFUNDED"),
    ENROLLMENT_CANNOT_BE_CANCELLED("ENROLLMENT_CANNOT_BE_CANCELLED",
            "ENROLLMENT_CANNOT_BE_CANCELLED");

    private final String code;
    private final String message;

    EnrollmentErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }
}
