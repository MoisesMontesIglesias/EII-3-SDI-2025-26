package com.uniovi.sdi.reservationmanagement.validators;

import com.uniovi.sdi.reservationmanagement.entities.Space;
import com.uniovi.sdi.reservationmanagement.entities.SpaceStatus;
import com.uniovi.sdi.reservationmanagement.services.SpaceService;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Component
public class AddSpaceValidator implements Validator {

    private static final int MAX_CAPACITY = 999;
    private static final List<String> SPACE_TYPES = List.of("SALA", "AULA", "COWORK");

    private final SpaceService spaceService;

    public AddSpaceValidator(SpaceService spaceService) {
        this.spaceService = spaceService;
    }

    @Override
    public boolean supports(@NonNull Class<?> aClass) {
        return Space.class.equals(aClass);
    }

    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {
        Space space = (Space) target;
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "Error.add.name.empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "type", "Error.add.type.empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "location", "Error.add.location.empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "status", "Error.add.status.empty");
        if (space.getCapacity() == null && !errors.hasFieldErrors("capacity")) {
            errors.rejectValue("capacity", "Error.add.capacity.empty");
        }

        String name = normalize(space.getName());
        if (name != null) {
            space.setName(name);
            Optional<Space> existing = spaceService.findByName(name);
            if (existing.isPresent() && existing.get().getStatus() == SpaceStatus.ACTIVE
                    && (space.getId() == null || !existing.get().getId().equals(space.getId()))) {
                errors.rejectValue("name", "Error.add.name.duplicate");
            }
        }

        String type = normalize(space.getType());
        if (type != null) {
            type = type.toUpperCase(Locale.ROOT);
            space.setType(type);
            if (!SPACE_TYPES.contains(type)) {
                errors.rejectValue("type", "Error.add.type.invalid");
            }
        }

        String location = normalize(space.getLocation());
        if (location != null) {
            space.setLocation(location);
        }

        String description = normalize(space.getDescription());
        space.setDescription(description);

        Integer capacity = space.getCapacity();
        if (capacity != null) {
            if (capacity < 1 || capacity > MAX_CAPACITY) {
                errors.rejectValue("capacity", "Error.add.capacity.invalid");
            }
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
