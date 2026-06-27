package com.uniovi.sdi.reservationmanagement.services;

import com.uniovi.sdi.reservationmanagement.entities.BlockStatus;
import com.uniovi.sdi.reservationmanagement.entities.MaintenanceBlock;
import com.uniovi.sdi.reservationmanagement.entities.Space;
import com.uniovi.sdi.reservationmanagement.repositories.MaintenanceBlockRepository;
import com.uniovi.sdi.reservationmanagement.repositories.SpaceRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MaintenanceBlockService {

    private final MaintenanceBlockRepository maintenanceBlockRepository;
    private final SpaceRepository spaceRepository;

    public MaintenanceBlockService(
            MaintenanceBlockRepository maintenanceBlockRepository,
            SpaceRepository spaceRepository
    ) {
        this.maintenanceBlockRepository = maintenanceBlockRepository;
        this.spaceRepository = spaceRepository;
    }

    public void createIfMissing(
            String spaceName,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            BlockStatus status,
            String reason
    ) {
        Space space = spaceRepository.findByNameIgnoreCase(spaceName).orElseThrow();
        boolean exists = maintenanceBlockRepository.existsBySpaceIdAndStartDateTimeAndEndDateTimeAndStatus(
                space.getId(), startDateTime, endDateTime, status
        );
        if (!exists) {
            maintenanceBlockRepository.save(new MaintenanceBlock(space, startDateTime, endDateTime, status, reason));
        }
    }

    public void createBlock(Space space, LocalDateTime startDateTime, LocalDateTime endDateTime, String reason) {
        MaintenanceBlock block = new MaintenanceBlock(space, startDateTime, endDateTime, BlockStatus.ACTIVE, reason);
        maintenanceBlockRepository.save(block);
    }

    public List<MaintenanceBlock> findBySpaceId(Long spaceId) {
        return maintenanceBlockRepository.findBySpaceIdOrderByStartDateTimeDesc(spaceId);
    }

    public void cancelBlock(Long spaceId, Long blockId) {
        Optional<MaintenanceBlock> blockOpt = maintenanceBlockRepository.findById(blockId);
        if (blockOpt.isEmpty()) {
            return;
        }
        MaintenanceBlock block = blockOpt.get();
        if (block.getSpace() == null || !block.getSpace().getId().equals(spaceId)) {
            return;
        }
        if (block.getStatus() != BlockStatus.ACTIVE) {
            return;
        }
        block.setStatus(BlockStatus.CANCELLED);
        maintenanceBlockRepository.save(block);
    }
}
