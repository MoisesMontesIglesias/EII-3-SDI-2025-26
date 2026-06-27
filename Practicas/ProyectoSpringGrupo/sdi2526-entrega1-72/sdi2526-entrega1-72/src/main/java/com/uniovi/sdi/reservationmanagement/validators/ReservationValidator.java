package com.uniovi.sdi.reservationmanagement.validators;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.time.LocalDateTime;

@Component
public class ReservationValidator implements Validator {

    @Override
    public boolean supports(@Nullable Class<?> clazz) {
        return true;
    }

    @Override
    public void validate(@Nullable Object target, @Nullable Errors errors) {
        // no-op, use the typed validate method
    }

    public void validate(
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            String reason,
            boolean recurringWeekly,
            Integer recurrenceWeeks,
            Errors errors
    ) {
        if (startDateTime == null) {
            errors.rejectValue("startDateTime", "startDateTime.empty");
        }
        if (endDateTime == null) {
            errors.rejectValue("endDateTime", "endDateTime.empty");
        }
        if (startDateTime != null && endDateTime != null && !endDateTime.isAfter(startDateTime)) {
            errors.rejectValue("endDateTime", "endDateTime.invalid");
        }
        if (startDateTime != null && endDateTime != null && endDateTime.isBefore(startDateTime.plusHours(1))) {
            errors.rejectValue("endDateTime", "endDateTime.minimumDuration");
        }
        if (reason != null && reason.length() > 250) {
            errors.rejectValue("reason", "reason.tooLong");
        }
        if (recurringWeekly) {
            if (recurrenceWeeks == null) {
                errors.rejectValue("recurrenceWeeks", "recurrenceWeeks.empty");
            } else if (recurrenceWeeks < 2 || recurrenceWeeks > 12) {
                errors.rejectValue("recurrenceWeeks", "recurrenceWeeks.invalid");
            }
        }
    }
}
