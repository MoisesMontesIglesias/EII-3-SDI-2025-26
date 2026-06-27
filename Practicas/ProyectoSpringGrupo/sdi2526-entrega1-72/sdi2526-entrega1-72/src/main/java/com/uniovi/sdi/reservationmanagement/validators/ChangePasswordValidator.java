package com.uniovi.sdi.reservationmanagement.validators;

import com.uniovi.sdi.reservationmanagement.entities.ChangePassword;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
public class ChangePasswordValidator implements Validator {

    @Override
    public boolean supports(@NonNull Class<?> clazz) {
        return ChangePassword.class.equals(clazz);
    }

    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "currentPassword", "password.current.required");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "newPassword", "password.new.required");
    }
}
