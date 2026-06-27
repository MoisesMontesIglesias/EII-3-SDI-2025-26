package com.uniovi.sdi.reservationmanagement.validators;

import com.uniovi.sdi.reservationmanagement.entities.User;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class UserLoginValidator implements Validator {

    @Override
    public boolean supports(@NonNull Class<?> clazz) {
        return User.class.equals(clazz);
    }

    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {
        User user = (User) target;
        if (user.getDni() == null || user.getDni().isBlank()) {
            errors.rejectValue("dni", "login.dni.required", "El DNI es obligatorio.");
        }
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            errors.rejectValue("password", "login.password.required", "La contrasena es obligatoria.");
        }
    }
}
