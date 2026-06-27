package com.uniovi.sdi.reservationmanagement.validators;

import com.uniovi.sdi.reservationmanagement.entities.BlockStatus;
import com.uniovi.sdi.reservationmanagement.entities.MaintenanceBlock;
import com.uniovi.sdi.reservationmanagement.entities.Reservation;
import com.uniovi.sdi.reservationmanagement.entities.ReservationStatus;
import com.uniovi.sdi.reservationmanagement.repositories.MaintenanceBlockRepository;
import com.uniovi.sdi.reservationmanagement.repositories.ReservationRepository;
import com.uniovi.sdi.reservationmanagement.services.OccupiedSlot;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class MaintenanceBlockValidator implements Validator {

    private final MaintenanceBlockRepository maintenanceBlockRepository;
    private final ReservationRepository reservationRepository;

    public MaintenanceBlockValidator(
            MaintenanceBlockRepository maintenanceBlockRepository,
            ReservationRepository reservationRepository
    ) {
        this.maintenanceBlockRepository = maintenanceBlockRepository;
        this.reservationRepository = reservationRepository;
    }

    @Override
    public boolean supports(@NonNull Class<?> clazz) {
        return MaintenanceBlock.class.equals(clazz);
    }

    public void validate(MaintenanceBlock form, Errors errors, Long spaceId) {
        validate(form, errors);
        if (errors.hasErrors()) {
            return;
        }
        LocalDateTime start = form.getStartDateTime();
        LocalDateTime end = form.getEndDateTime();
        if (start == null || end == null) {
            return;
        }
        if (!start.isBefore(end)) {
            errors.rejectValue("startDateTime", "block.start.after.end", "El inicio debe ser anterior al fin.");
            return;
        }
        if (start.isBefore(LocalDateTime.now())) {
            errors.rejectValue("startDateTime", "block.start.past", "El inicio no puede estar en el pasado.");
            return;
        }

        boolean overlapsBlock = maintenanceBlockRepository
                .existsBySpaceIdAndStatusAndEndDateTimeAfterAndStartDateTimeBefore(
                        spaceId, BlockStatus.ACTIVE, start, end
                );
        if (overlapsBlock) {
            errors.rejectValue("startDateTime", "block.overlap.block", "Se solapa con otro bloqueo activo.");
            return;
        }

        boolean overlapsReservation = reservationRepository
                .existsBySpaceIdAndStatusAndEndDateTimeAfterAndStartDateTimeBefore(
                        spaceId, ReservationStatus.ACTIVE, start, end
                );
        if (overlapsReservation) {
            errors.rejectValue("startDateTime", "block.overlap.reservation", "Se solapa con una reserva activa.");
        }
    }

    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "startDateTime", "block.start.required", "El inicio es obligatorio.");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "endDateTime", "block.end.required", "El fin es obligatorio.");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "reason", "block.reason.required", "La descripcion es obligatoria.");
    }

    public List<OccupiedSlot> findCollisionSlots(Long spaceId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        List<Reservation> reservations = reservationRepository
                .findBySpaceIdAndStatusAndEndDateTimeAfterAndStartDateTimeBeforeOrderByStartDateTimeAsc(
                        spaceId, ReservationStatus.ACTIVE, startDateTime, endDateTime
                );
        List<MaintenanceBlock> blocks = maintenanceBlockRepository
                .findBySpaceIdAndStatusAndEndDateTimeAfterAndStartDateTimeBeforeOrderByStartDateTimeAsc(
                        spaceId, BlockStatus.ACTIVE, startDateTime, endDateTime
                );

        List<OccupiedSlot> collisions = new ArrayList<>();
        collisions.addAll(buildReservationCollisions(reservations));
        collisions.addAll(buildBlockCollisions(blocks));

        collisions.sort(Comparator.comparing(OccupiedSlot::startDateTime));
        return collisions;
    }

    private List<OccupiedSlot> buildReservationCollisions(List<Reservation> reservations) {
        List<OccupiedSlot> collisions = new ArrayList<>();
        for (Reservation reservation : reservations) {
            collisions.add(new OccupiedSlot(
                    reservation.getStartDateTime(),
                    reservation.getEndDateTime(),
                    "RESERVA",
                    reservation.getReason() == null || reservation.getReason().isBlank()
                            ? "Reserva activa"
                            : reservation.getReason()
            ));
        }
        return collisions;
    }

    private List<OccupiedSlot> buildBlockCollisions(List<MaintenanceBlock> blocks) {
        List<OccupiedSlot> collisions = new ArrayList<>();
        for (MaintenanceBlock block : blocks) {
            collisions.add(new OccupiedSlot(
                    block.getStartDateTime(),
                    block.getEndDateTime(),
                    "BLOQUEO",
                    block.getReason()
            ));
        }
        return collisions;
    }
}
