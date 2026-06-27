package com.uniovi.sdi.reservationmanagement.services;

import com.uniovi.sdi.reservationmanagement.entities.Space;
import com.uniovi.sdi.reservationmanagement.entities.MaintenanceBlock;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class SpaceDetailService {

    private final SpaceService spaceService;
    private final SpaceAvailabilityService spaceAvailabilityService;
    private final MaintenanceBlockService maintenanceBlockService;

    public SpaceDetailService(
            SpaceService spaceService,
            SpaceAvailabilityService spaceAvailabilityService,
            MaintenanceBlockService maintenanceBlockService
    ) {
        this.spaceService = spaceService;
        this.spaceAvailabilityService = spaceAvailabilityService;
        this.maintenanceBlockService = maintenanceBlockService;
    }

    public Optional<SpaceDetailResult> buildDetail(Long spaceId, LocalDate dateFrom, LocalDate dateTo, boolean isAdmin) {
        Optional<Space> space = isAdmin ? spaceService.findById(spaceId) : spaceService.findActiveById(spaceId);
        if (space.isEmpty()) {
            return Optional.empty();
        }

        LocalDate normalizedDateFrom = dateFrom == null ? LocalDate.now() : dateFrom;
        LocalDate normalizedDateTo = dateTo == null ? normalizedDateFrom.plusDays(6) : dateTo;
        if (normalizedDateTo.isBefore(normalizedDateFrom)) {
            normalizedDateTo = normalizedDateFrom;
        }

        List<OccupiedSlot> occupiedSlots = spaceAvailabilityService.findOccupiedSlots(
                spaceId,
                normalizedDateFrom,
                normalizedDateTo
        );

        List<MaintenanceBlock> maintenanceBlocks = maintenanceBlockService.findBySpaceId(spaceId);

        return Optional.of(new SpaceDetailResult(
                space.get(),
                normalizedDateFrom,
                normalizedDateTo,
                occupiedSlots,
                maintenanceBlocks
        ));
    }

    public Optional<SpaceDetailResult> buildDetailForCurrentUser(Long spaceId, LocalDate dateFrom, LocalDate dateTo) {
        var authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication();
        boolean isAdmin = authentication != null
                && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        if (!isAdmin) {
            var attributes = RequestContextHolder.getRequestAttributes();
            if (attributes instanceof ServletRequestAttributes servletAttributes) {
                var session = servletAttributes.getRequest().getSession(false);
                if (session != null) {
                    Object role = session.getAttribute("role");
                    isAdmin = "ADMIN".equals(role);
                }
            }
        }
        return buildDetail(spaceId, dateFrom, dateTo, isAdmin);
    }

    public record SpaceDetailResult(
            Space space,
            LocalDate dateFrom,
            LocalDate dateTo,
            List<OccupiedSlot> occupiedSlots,
            List<MaintenanceBlock> maintenanceBlocks
    ) {
    }
}
