package com.uniovi.sdi.reservationmanagement.validators;

import com.uniovi.sdi.reservationmanagement.entities.User;
import com.uniovi.sdi.reservationmanagement.services.UserService;
import org.jspecify.annotations.NonNull;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.LengthRule;
import org.passay.PasswordData;
import org.passay.PasswordValidator;
import org.passay.RuleResult;
import org.passay.WhitespaceRule;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class UserSignUpValidator implements Validator {

    private final UserService userService;
    private final PasswordValidator passwordValidator;

    public UserSignUpValidator(UserService userService) {
        this.userService = userService;
        this.passwordValidator = new PasswordValidator(
                new LengthRule(12, 20),
                new CharacterRule(EnglishCharacterData.UpperCase, 1),
                new CharacterRule(EnglishCharacterData.LowerCase, 1),
                new CharacterRule(EnglishCharacterData.Digit, 1),
                new CharacterRule(EnglishCharacterData.Special, 1),
                new WhitespaceRule()
        );
    }

    @Override
    public boolean supports(@NonNull Class<?> clazz) {
        return User.class.equals(clazz);
    }

    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {
        User user = (User) target;

        // DNI: solo un mensaje de error por envio
        if (isBlank(user.getDni())) {
            errors.rejectValue("dni", "signup.dni.required", "El DNI es obligatorio.");
        } else if (!isValidDni(user.getDni())) {
            errors.rejectValue("dni", "signup.dni.invalid", "El DNI no es valido.");
        } else if (userService.existsByDni(user.getDni())) {
            errors.rejectValue("dni", "signup.dni.duplicate", "Ya existe un usuario con ese DNI.");
        }

        if (isBlank(user.getName())) {
            errors.rejectValue("name", "signup.name.required", "El nombre es obligatorio.");
        }
        if (isBlank(user.getLastName())) {
            errors.rejectValue("lastName", "signup.lastName.required", "Los apellidos son obligatorios.");
        }

        // Contrasena: solo un mensaje de error por envio
        if (isBlank(user.getPassword())) {
            errors.rejectValue("password", "signup.password.required", "La contrasena es obligatoria.");
        } else {
            RuleResult result = passwordValidator.validate(new PasswordData(user.getPassword()));
            if (!result.isValid()) {
                errors.rejectValue("password", "signup.password.invalid", "La contrasena no cumple los requisitos.");
            }
        }

        if (isBlank(user.getPasswordConfirm())) {
            errors.rejectValue("passwordConfirm", "signup.passwordConfirm.required", "Debes confirmar la contrasena.");
        } else if (!isBlank(user.getPassword()) && !user.getPassword().equals(user.getPasswordConfirm())) {
            errors.rejectValue("passwordConfirm", "signup.password.confirm.mismatch", "Las contrasenas no coinciden.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private boolean isValidDni(String dni) {
        if (dni == null || !dni.matches("\\d{8}[A-Za-z]")) {
            return false;
        }
        String letters = "TRWAGMYFPDXBNJZSQVHLCKE";
        int number = Integer.parseInt(dni.substring(0, 8));
        char expected = letters.charAt(number % 23);
        char provided = Character.toUpperCase(dni.charAt(8));
        return expected == provided;
    }
}
