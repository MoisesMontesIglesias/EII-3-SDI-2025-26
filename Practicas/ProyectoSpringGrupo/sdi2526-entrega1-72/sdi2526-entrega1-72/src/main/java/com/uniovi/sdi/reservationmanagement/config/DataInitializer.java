package com.uniovi.sdi.reservationmanagement.config;

import com.uniovi.sdi.reservationmanagement.entities.BlockStatus;
import com.uniovi.sdi.reservationmanagement.entities.ReservationStatus;
import com.uniovi.sdi.reservationmanagement.entities.SpaceStatus;
import com.uniovi.sdi.reservationmanagement.entities.User;
import com.uniovi.sdi.reservationmanagement.services.MaintenanceBlockService;
import com.uniovi.sdi.reservationmanagement.services.ReservationService;
import com.uniovi.sdi.reservationmanagement.services.SpaceService;
import com.uniovi.sdi.reservationmanagement.services.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(
            UserService userService,
            SpaceService spaceService,
            ReservationService reservationService,
            MaintenanceBlockService maintenanceBlockService
    ) {
        return args -> {
            @SuppressWarnings("unused")
            String[] unusedArgs = args;
            userService.registerAdminIfAbsent(new User("12345678Z", "Admin", "Sistema", "@Dm1n1str@D0r"));
            createStandardUsers(userService);
            Map<String, Integer> seededReservations = new HashMap<>();

            spaceService.createIfMissing(
                    "Sala Norte",
                    "SALA",
                    "Edificio A - Planta 1",
                    8,
                    SpaceStatus.ACTIVE,
                    "Sala amplia con pantalla y pizarra para reuniones."
            );
            spaceService.createIfMissing(
                    "Aula 2.1",
                    "AULA",
                    "Edificio B - Planta 2",
                    25,
                    SpaceStatus.ACTIVE,
                    "Aula con proyector y 25 puestos, ideal para clases."
            );
            spaceService.createIfMissing(
                    "Aula 3.2",
                    "AULA",
                    "Edificio B - Planta 3",
                    30,
                    SpaceStatus.ACTIVE,
                    "Aula luminosa para grupos medianos."
            );
            spaceService.createIfMissing(
                    "Cowork 05",
                    "COWORK",
                    "Edificio C - Zona cowork",
                    1,
                    SpaceStatus.ACTIVE,
                    "Puesto individual en zona cowork silenciosa."
            );
            spaceService.createIfMissing(
                    "Sala Sur",
                    "SALA",
                    "Edificio A - Planta 0",
                    10,
                    SpaceStatus.CANCELLED,
                    "Sala compacta para reuniones pequeñas."
            );
            spaceService.createIfMissing(
                    "Cowork 12",
                    "COWORK",
                    "Edificio C - Zona cowork",
                    1,
                    SpaceStatus.ACTIVE,
                    "Puesto individual con monitor y conexión cableada."
            );

            LocalDate baseDate = LocalDate.now().plusDays(1);
            LocalDateTime morningStart = baseDate.atTime(9, 0);
            LocalDateTime morningEnd = baseDate.atTime(11, 0);
            LocalDateTime noonStart = baseDate.atTime(12, 0);
            LocalDateTime noonEnd = baseDate.atTime(13, 30);
            LocalDateTime eveningStart = baseDate.plusDays(1).atTime(16, 0);
            LocalDateTime eveningEnd = baseDate.plusDays(1).atTime(18, 0);
            LocalDate pastDate = LocalDate.now().minusDays(5);

            createReservationIfUnderLimit(
                    seededReservations,
                    reservationService,
                    "Sala Norte",
                    "10000001S",
                    morningStart,
                    morningEnd,
                    ReservationStatus.ACTIVE,
                    "Reunion de planificacion"
            );
            createReservationIfUnderLimit(
                    seededReservations,
                    reservationService,
                    "Sala Norte",
                    "10000002Q",
                    eveningStart,
                    eveningEnd,
                    ReservationStatus.CANCELLED,
                    "Reserva historica cancelada"
            );
            createReservationIfUnderLimit(
                    seededReservations,
                    reservationService,
                    "Aula 2.1",
                    "10000003V",
                    noonStart,
                    noonEnd,
                    ReservationStatus.ACTIVE,
                    "Clase de repaso"
            );
            createReservationIfUnderLimit(
                    seededReservations,
                    reservationService,
                    "Aula 3.2",
                    "10000004L",
                    baseDate.atTime(8, 30),
                    baseDate.atTime(10, 30),
                    ReservationStatus.ACTIVE,
                    "Sesión de prácticas"
            );
            createReservationIfUnderLimit(
                    seededReservations,
                    reservationService,
                    "Cowork 05",
                    "10000001S",
                    baseDate.plusDays(2).atTime(9, 0),
                    baseDate.plusDays(2).atTime(10, 0),
                    ReservationStatus.ACTIVE,
                    "Trabajo concentrado"
            );
            createReservationIfUnderLimit(
                    seededReservations,
                    reservationService,
                    "Sala Norte",
                    "10000002Q",
                    baseDate.plusDays(3).atTime(14, 0),
                    baseDate.plusDays(3).atTime(15, 0),
                    ReservationStatus.ACTIVE,
                    "Seguimiento sprint"
            );
            createReservationIfUnderLimit(
                    seededReservations,
                    reservationService,
                    "Aula 2.1",
                    "10000003V",
                    baseDate.plusDays(4).atTime(10, 0),
                    baseDate.plusDays(4).atTime(11, 30),
                    ReservationStatus.CANCELLED,
                    "Reserva cancelada de prueba"
            );
            seedReservationPacks(reservationService, baseDate, seededReservations);

            maintenanceBlockService.createIfMissing(
                    "Sala Norte",
                    baseDate.atTime(15, 0),
                    baseDate.atTime(16, 0),
                    BlockStatus.ACTIVE,
                    "Bloqueo operativo base"
            );
            maintenanceBlockService.createIfMissing(
                    "Sala Norte",
                    LocalDate.of(2027, 2, 15).atTime(15, 0),
                    LocalDate.of(2027, 2, 15).atTime(16, 30),
                    BlockStatus.ACTIVE,
                    "Mantenimiento del proyector"
            );
            maintenanceBlockService.createIfMissing(
                    "Sala Norte",
                    LocalDate.of(2027, 3, 10).atTime(8, 0),
                    LocalDate.of(2027, 3, 10).atTime(9, 30),
                    BlockStatus.ACTIVE,
                    "Revision de aire acondicionado"
            );
            maintenanceBlockService.createIfMissing(
                    "Aula 2.1",
                    pastDate.atTime(9, 0),
                    pastDate.atTime(10, 0),
                    BlockStatus.CANCELLED,
                    "Bloqueo cancelado"
            );
            maintenanceBlockService.createIfMissing(
                    "Aula 2.1",
                    LocalDate.of(2027, 4, 5).atTime(12, 0),
                    LocalDate.of(2027, 4, 5).atTime(13, 0),
                    BlockStatus.ACTIVE,
                    "Sustitucion de mobiliario"
            );
            maintenanceBlockService.createIfMissing(
                    "Cowork 05",
                    LocalDate.of(2027, 5, 20).atTime(10, 0),
                    LocalDate.of(2027, 5, 20).atTime(12, 0),
                    BlockStatus.ACTIVE,
                    "Inspeccion de seguridad"
            );
        };
    }

    private void createStandardUsers(UserService userService) {
        registerIfMissing(userService, "10000001S", "Us3r@1-PASSW", "Ana", "Suarez");
        registerIfMissing(userService, "10000002Q", "Us3r@2-PASSW", "Luis", "Quintana");
        registerIfMissing(userService, "10000003V", "Us3r@3-PASSW", "Marta", "Vega");
        registerIfMissing(userService, "10000004L", "Us3r@4-PASSW", "Pablo", "Lopez");
        registerIfMissing(userService, "10000005M", "Us3r@5-PASSW", "Elena", "Moreno");
    }

    private void registerIfMissing(UserService userService, String dni, String password, String name, String lastName) {
        if (!userService.existsByDni(dni)) {
            userService.registerStandardUser(new User(dni, name, lastName, password));
        }
    }

    private void seedReservationPacks(
            ReservationService reservationService,
            LocalDate baseDate,
            Map<String, Integer> seededReservations
    ) {
        createReservationPack(
                reservationService,
                seededReservations,
                "10000001S",
                new String[] { "Sala Norte", "Cowork 05", "Aula 3.2", "Cowork 12", "Aula 2.1" },
                baseDate.plusDays(6),
                "Reserva usuario 1"
        );
        createReservationPack(
                reservationService,
                seededReservations,
                "10000002Q",
                new String[] { "Aula 3.2", "Cowork 12", "Sala Norte", "Aula 2.1", "Cowork 05" },
                baseDate.plusDays(12),
                "Reserva usuario 2"
        );
        createReservationPack(
                reservationService,
                seededReservations,
                "10000003V",
                new String[] { "Aula 2.1", "Sala Norte", "Cowork 12", "Aula 3.2", "Cowork 05" },
                baseDate.plusDays(18),
                "Reserva usuario 3"
        );
        createReservationPack(
                reservationService,
                seededReservations,
                "10000004L",
                new String[] { "Cowork 05", "Aula 3.2", "Sala Norte", "Cowork 12", "Aula 2.1" },
                baseDate.plusDays(24),
                "Reserva usuario 4"
        );
        createReservationPack(
                reservationService,
                seededReservations,
                "10000005M",
                new String[] { "Cowork 12", "Sala Norte", "Aula 2.1", "Cowork 05", "Aula 3.2" },
                baseDate.plusDays(30),
                "Reserva usuario 5"
        );
    }

    private void createReservationPack(
            ReservationService reservationService,
            Map<String, Integer> seededReservations,
            String userDni,
            String[] spaces,
            LocalDate startDate,
            String reasonPrefix
    ) {
        for (int i = 0; i < spaces.length; i++) {
            LocalDateTime start = startDate.plusDays(i).atTime(9, 0);
            LocalDateTime end = start.plusHours(1);
            createReservationIfUnderLimit(
                    seededReservations,
                    reservationService,
                    spaces[i],
                    userDni,
                    start,
                    end,
                    ReservationStatus.ACTIVE,
                    reasonPrefix + " " + (i + 1)
            );
        }
    }

    private void createReservationIfUnderLimit(
            Map<String, Integer> seededReservations,
            ReservationService reservationService,
            String spaceName,
            String userDni,
            LocalDateTime start,
            LocalDateTime end,
            ReservationStatus status,
            String reason
    ) {
        int current = seededReservations.getOrDefault(userDni, 0);
        if (current >= 3) {
            return;
        }
        reservationService.createIfMissing(spaceName, userDni, start, end, status, reason);
        seededReservations.put(userDni, current + 1);
    }
}
