package com.uniovi.sdi.reservationmanagement.services;

import com.uniovi.sdi.reservationmanagement.entities.BlockStatus;
import com.uniovi.sdi.reservationmanagement.entities.MaintenanceBlock;
import com.uniovi.sdi.reservationmanagement.entities.Reservation;
import com.uniovi.sdi.reservationmanagement.entities.ReservationStatus;
import com.uniovi.sdi.reservationmanagement.repositories.MaintenanceBlockRepository;
import com.uniovi.sdi.reservationmanagement.repositories.ReservationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class SpaceAvailabilityService {

    private final ReservationRepository reservationRepository;
    private final MaintenanceBlockRepository maintenanceBlockRepository;

    public SpaceAvailabilityService(
            ReservationRepository reservationRepository,
            MaintenanceBlockRepository maintenanceBlockRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.maintenanceBlockRepository = maintenanceBlockRepository;
    }

    public List<OccupiedSlot> findOccupiedSlots(Long spaceId, LocalDate dateFrom, LocalDate dateTo) {
        LocalDateTime start = dateFrom.atStartOfDay();
        LocalDateTime endExclusive = dateTo.plusDays(1).atStartOfDay();

        List<Reservation> activeReservations = reservationRepository
                .findBySpaceIdAndStatusAndEndDateTimeAfterAndStartDateTimeBeforeOrderByStartDateTimeAsc(
                        spaceId, ReservationStatus.ACTIVE, start, endExclusive
                );
        List<MaintenanceBlock> blocks = maintenanceBlockRepository
                .findBySpaceIdAndStatusAndEndDateTimeAfterAndStartDateTimeBeforeOrderByStartDateTimeAsc(
                        spaceId, BlockStatus.ACTIVE, start, endExclusive
                );
        List<OccupiedSlot> occupiedSlots = buildOccupiedSlots(activeReservations, blocks, LocalDateTime.now());
        occupiedSlots.sort(Comparator.comparing(OccupiedSlot::startDateTime));
        return occupiedSlots;
    }

    private List<OccupiedSlot> buildOccupiedSlots(
            List<Reservation> activeReservations,
            List<MaintenanceBlock> blocks,
            LocalDateTime now
    ) {
        List<OccupiedSlot> occupiedSlots = new ArrayList<>();
        for (Reservation reservation : activeReservations) {
            occupiedSlots.add(new OccupiedSlot(
                    reservation.getStartDateTime(),
                    reservation.getEndDateTime(),
                    "RESERVA_ACTIVA",
                    reservation.getReason() == null || reservation.getReason().isBlank()
                            ? "Reserva activa"
                            : reservation.getReason()
            ));
        }
        for (MaintenanceBlock block : blocks) {
            if (block.getEndDateTime() != null && block.getEndDateTime().isBefore(now)) {
                continue;
            }
            occupiedSlots.add(new OccupiedSlot(
                    block.getStartDateTime(),
                    block.getEndDateTime(),
                    "BLOQUEO",
                    block.getReason()
            ));
        }
        return occupiedSlots;
    }
}
