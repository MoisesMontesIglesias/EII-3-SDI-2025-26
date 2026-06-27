package com.uniovi.sdi.reservationmanagement.repositories;

import com.uniovi.sdi.reservationmanagement.entities.Reservation;
import com.uniovi.sdi.reservationmanagement.entities.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findBySpaceIdAndStatusAndEndDateTimeAfterAndStartDateTimeBeforeOrderByStartDateTimeAsc(
            Long spaceId,
            ReservationStatus status,
            LocalDateTime start,
            LocalDateTime end
    );

    List<Reservation> findByUserDniOrderByStartDateTimeDesc(String dni);

    Optional<Reservation> findByIdAndUserDni(Long id, String dni);

    boolean existsBySpaceIdAndUserDniAndStartDateTimeAndEndDateTimeAndStatus(
            Long spaceId,
            String dni,
            LocalDateTime start,
            LocalDateTime end,
            ReservationStatus status
    );

    long countByUserDniAndStatus(String dni, ReservationStatus status);

    boolean existsBySpaceIdAndStatusAndEndDateTimeAfterAndStartDateTimeBefore(
            Long spaceId,
            ReservationStatus status,
            LocalDateTime start,
            LocalDateTime end
    );
}
