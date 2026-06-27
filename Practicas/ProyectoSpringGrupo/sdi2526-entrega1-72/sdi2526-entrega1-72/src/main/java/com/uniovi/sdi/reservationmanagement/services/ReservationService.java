package com.uniovi.sdi.reservationmanagement.services;

import com.uniovi.sdi.reservationmanagement.entities.Reservation;
import com.uniovi.sdi.reservationmanagement.entities.ReservationStatus;
import com.uniovi.sdi.reservationmanagement.entities.Space;
import com.uniovi.sdi.reservationmanagement.entities.SpaceStatus;
import com.uniovi.sdi.reservationmanagement.entities.User;
import com.uniovi.sdi.reservationmanagement.entities.BlockStatus;
import com.uniovi.sdi.reservationmanagement.repositories.MaintenanceBlockRepository;
import com.uniovi.sdi.reservationmanagement.repositories.ReservationRepository;
import com.uniovi.sdi.reservationmanagement.repositories.SpaceRepository;
import com.uniovi.sdi.reservationmanagement.repositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReservationService {

    private static final int MAX_ACTIVE_RESERVATIONS_PER_USER = 5;
    private static final int MAX_WEEKLY_OCCURRENCES = 12;

    private final ReservationRepository reservationRepository;
    private final SpaceRepository spaceRepository;
    private final UserRepository userRepository;
    private final MaintenanceBlockRepository maintenanceBlockRepository;

    public ReservationService(
            ReservationRepository reservationRepository,
            SpaceRepository spaceRepository,
            UserRepository userRepository,
            MaintenanceBlockRepository maintenanceBlockRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.spaceRepository = spaceRepository;
        this.userRepository = userRepository;
        this.maintenanceBlockRepository = maintenanceBlockRepository;
    }

    public void createIfMissing(
            String spaceName,
            String userDni,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            ReservationStatus status,
            String reason
    ) {
        Space space = spaceRepository.findByNameIgnoreCase(spaceName).orElseThrow();
        if (space.getStatus() != SpaceStatus.ACTIVE) {
            return;
        }
        User user = userRepository.findByDni(userDni).orElseThrow();
        boolean exists = reservationRepository.existsBySpaceIdAndUserDniAndStartDateTimeAndEndDateTimeAndStatus(
                space.getId(), userDni, startDateTime, endDateTime, status
        );
        if (!exists) {
            reservationRepository.save(new Reservation(space, user, startDateTime, endDateTime, status, reason));
        }
    }

    public Page<ReservationRow> findGlobalReservations(Long spaceId, LocalDate dateFrom, LocalDate dateTo, int page, int size) {
        List<Reservation> reservations = reservationRepository.findAll(Sort.by(Sort.Direction.ASC, "startDateTime"));
        List<ReservationRow> filtered = filterReservations(reservations, spaceId, dateFrom, dateTo);

        Pageable pageable = PageRequest.of(page, size);
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtered.size());
        List<ReservationRow> pageContent = start > filtered.size() ? List.of() : filtered.subList(start, end);

        return new PageImpl<>(pageContent, pageable, filtered.size());
    }

    public List<ReservationRow> findGlobalReservations(Long spaceId, LocalDate dateFrom, LocalDate dateTo) {
        List<Reservation> reservations = reservationRepository.findAll(Sort.by(Sort.Direction.ASC, "startDateTime"));
        return filterReservations(reservations, spaceId, dateFrom, dateTo);
    }

    public List<UserReservationRow> findUserReservations(String userDni, String status) {
        List<Reservation> reservations = reservationRepository.findByUserDniOrderByStartDateTimeDesc(userDni);
        return reservations.stream()
                .filter(reservation -> status == null || status.isBlank() || reservation.getStatus().name().equals(status))
                .map(reservation -> new UserReservationRow(
                        reservation.getId(),
                        reservation.getSpace().getName(),
                        reservation.getStartDateTime(),
                        reservation.getEndDateTime(),
                        reservation.getStatus().name(),
                        reservation.getReason()
                ))
                .collect(Collectors.toList());
    }

    public ReservationActionResult cancelReservation(Long reservationId, String userDni) {
        Reservation reservation = reservationRepository.findByIdAndUserDni(reservationId, userDni).orElse(null);
        if (reservation == null) {
            return ReservationActionResult.fail("reservation.cancel.notFound");
        }
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            return ReservationActionResult.fail("reservation.cancel.alreadyCancelled");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
        return ReservationActionResult.ok();
    }

    public ReservationCreationResult createReservation(
            Long spaceId,
            String userDni,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            String reason,
            boolean recurringWeekly,
            Integer recurrenceWeeks
    ) {
        if (startDateTime == null || endDateTime == null) {
            return ReservationCreationResult.fail("reservation.error.datetime.required");
        }
        if (!endDateTime.isAfter(startDateTime)) {
            return ReservationCreationResult.fail("reservation.error.datetime.order");
        }
        if (startDateTime.isBefore(LocalDateTime.now())) {
            return ReservationCreationResult.fail("reservation.error.datetime.past");
        }
        if (endDateTime.isBefore(startDateTime.plusHours(1))) {
            return ReservationCreationResult.fail("reservation.error.datetime.minimumDuration");
        }

        Space space = spaceRepository.findByIdAndStatus(spaceId, SpaceStatus.ACTIVE).orElse(null);
        if (space == null) {
            return ReservationCreationResult.fail("reservation.error.space.unavailable");
        }

        User user = userRepository.findByDni(userDni).orElse(null);
        if (user == null) {
            return ReservationCreationResult.fail("reservation.error.user.missing");
        }

        String normalizedReason = reason == null ? null : reason.trim();
        if (normalizedReason != null && normalizedReason.length() > 250) {
            return ReservationCreationResult.fail("reservation.error.reason.tooLong");
        }

        int totalOccurrences = recurringWeekly ? recurrenceWeeks == null ? 0 : recurrenceWeeks : 1;
        if (totalOccurrences < 1 || totalOccurrences > MAX_WEEKLY_OCCURRENCES) {
            return ReservationCreationResult.fail("reservation.error.recurrence.invalid");
        }

        List<ReservationCandidate> candidates = buildCandidates(startDateTime, endDateTime, totalOccurrences);
        String normalizedStoredReason = normalizedReason == null || normalizedReason.isBlank() ? null : normalizedReason;

        for (ReservationCandidate candidate : candidates) {
            boolean hasReservations = !reservationRepository
                    .findBySpaceIdAndStatusAndEndDateTimeAfterAndStartDateTimeBeforeOrderByStartDateTimeAsc(
                            spaceId, ReservationStatus.ACTIVE, candidate.startDateTime(), candidate.endDateTime()
                    ).isEmpty();
            if (hasReservations) {
                return ReservationCreationResult.fail("reservation.error.space.hasReservation");
            }

            boolean hasBlocks = !maintenanceBlockRepository
                    .findBySpaceIdAndStatusAndEndDateTimeAfterAndStartDateTimeBeforeOrderByStartDateTimeAsc(
                            spaceId, BlockStatus.ACTIVE, candidate.startDateTime(), candidate.endDateTime()
                    ).isEmpty();
            if (hasBlocks) {
                return ReservationCreationResult.fail("reservation.error.space.blocked");
            }
        }

        long activeReservations = reservationRepository.countByUserDniAndStatus(userDni, ReservationStatus.ACTIVE);
        if (activeReservations + totalOccurrences > MAX_ACTIVE_RESERVATIONS_PER_USER) {
            return ReservationCreationResult.fail("reservation.error.user.limit");
        }

        for (ReservationCandidate candidate : candidates) {
            reservationRepository.save(new Reservation(
                    space,
                    user,
                    candidate.startDateTime(),
                    candidate.endDateTime(),
                    ReservationStatus.ACTIVE,
                    normalizedStoredReason
            ));
        }

        return ReservationCreationResult.ok();
    }

    private List<ReservationCandidate> buildCandidates(
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            int totalOccurrences
    ) {
        return java.util.stream.IntStream.range(0, totalOccurrences)
                .mapToObj(weekOffset -> new ReservationCandidate(
                        startDateTime.plusWeeks(weekOffset),
                        endDateTime.plusWeeks(weekOffset)
                ))
                .toList();
    }

    private boolean matchesSpaceFilter(Reservation reservation, Long spaceId) {
        return spaceId == null || reservation.getSpace().getId().equals(spaceId);
    }

    private boolean matchesDateRangeFilter(Reservation reservation, LocalDate dateFrom, LocalDate dateTo) {
        LocalDate reservationDate = reservation.getStartDateTime().toLocalDate();
        return (dateFrom == null || !reservationDate.isBefore(dateFrom))
                && (dateTo == null || !reservationDate.isAfter(dateTo));
    }

    private List<ReservationRow> filterReservations(List<Reservation> reservations, Long spaceId, LocalDate dateFrom, LocalDate dateTo) {
        return reservations.stream()
                .filter(reservation -> matchesSpaceFilter(reservation, spaceId))
                .filter(reservation -> matchesDateRangeFilter(reservation, dateFrom, dateTo))
                .map(reservation -> new ReservationRow(
                        formatUserLabel(reservation.getUser().getName(), reservation.getUser().getLastName()),
                        reservation.getSpace().getId(),
                        reservation.getSpace().getName(),
                        reservation.getStartDateTime(),
                        reservation.getEndDateTime(),
                        reservation.getStatus(),
                        reservation.getReason()
                ))
                .collect(Collectors.toList());
    }

    public record ReservationRow(
            String userLabel,
            Long spaceId,
            String spaceName,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            ReservationStatus status,
            String reason
    ) {
    }

    private String formatUserLabel(String name, String lastName) {
        String safeName = name == null ? "" : name.trim();
        String safeLastName = lastName == null ? "" : lastName.trim();
        String label = (safeName + " " + safeLastName).trim();
        return label.isBlank() ? "Sin nombre" : label;
    }

    public record UserReservationRow(
            Long id,
            String spaceName,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            String status,
            String reason
    ) {
    }

    public static class ReservationActionResult {
        private final boolean success;
        private final String message;

        private ReservationActionResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public static ReservationActionResult ok() {
            return new ReservationActionResult(true, null);
        }

        public static ReservationActionResult fail(String message) {
            return new ReservationActionResult(false, message);
        }
    }

    public static class ReservationCreationResult {
        private final boolean success;
        private final String message;

        private ReservationCreationResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public static ReservationCreationResult ok() {
            return new ReservationCreationResult(true, null);
        }

        public static ReservationCreationResult fail(String message) {
            return new ReservationCreationResult(false, message);
        }
    }

    private record ReservationCandidate(LocalDateTime startDateTime, LocalDateTime endDateTime) {
    }
}
