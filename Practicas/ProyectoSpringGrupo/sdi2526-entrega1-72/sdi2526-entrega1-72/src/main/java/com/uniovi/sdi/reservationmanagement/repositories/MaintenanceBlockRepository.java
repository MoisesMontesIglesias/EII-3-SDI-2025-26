package com.uniovi.sdi.reservationmanagement.repositories;

import com.uniovi.sdi.reservationmanagement.entities.BlockStatus;
import com.uniovi.sdi.reservationmanagement.entities.MaintenanceBlock;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MaintenanceBlockRepository extends CrudRepository<MaintenanceBlock, Long> {

    List<MaintenanceBlock> findBySpaceIdAndStatusAndEndDateTimeAfterAndStartDateTimeBeforeOrderByStartDateTimeAsc(
            Long spaceId,
            BlockStatus status,
            LocalDateTime start,
            LocalDateTime end
    );

    boolean existsBySpaceIdAndStartDateTimeAndEndDateTimeAndStatus(
            Long spaceId,
            LocalDateTime start,
            LocalDateTime end,
            BlockStatus status
    );

    boolean existsBySpaceIdAndStatusAndEndDateTimeAfterAndStartDateTimeBefore(
            Long spaceId,
            BlockStatus status,
            LocalDateTime start,
            LocalDateTime end
    );

    List<MaintenanceBlock> findBySpaceIdOrderByStartDateTimeDesc(Long spaceId);
}
