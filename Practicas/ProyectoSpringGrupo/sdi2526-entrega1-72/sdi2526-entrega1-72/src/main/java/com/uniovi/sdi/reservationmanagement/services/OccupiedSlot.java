package com.uniovi.sdi.reservationmanagement.services;

import java.time.LocalDateTime;

public record OccupiedSlot(
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        String source,
        String description
) {
}
