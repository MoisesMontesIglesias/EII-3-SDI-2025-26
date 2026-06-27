package com.uniovi.sdi.reservationmanagement.controllers;

import com.uniovi.sdi.reservationmanagement.entities.Space;
import com.uniovi.sdi.reservationmanagement.services.ReservationService;
import com.uniovi.sdi.reservationmanagement.services.SpaceService;
import com.uniovi.sdi.reservationmanagement.validators.ReservationValidator;
import com.uniovi.sdi.reservationmanagement.util.CsvUtils;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.HashMap;

@Controller
public class ReservationController {

    private static final int PAGE_SIZE = 5;
    private static final DateTimeFormatter DATETIME_LOCAL_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private final ReservationService reservationService;
    private final SpaceService spaceService;
    private final ReservationValidator reservationValidator;

    public ReservationController(
            ReservationService reservationService,
            SpaceService spaceService,
            ReservationValidator reservationValidator
    ) {
        this.reservationService = reservationService;
        this.spaceService = spaceService;
        this.reservationValidator = reservationValidator;
    }

    /**
     * Muestra el formulario de creacion de reservas para un espacio.
     *
     * @param spaceId identificador del espacio
     * @param dateFrom fecha inicial (opcional) para pre-rellenar
     * @param session sesion HTTP
     * @param model modelo de vista
     * @return vista de creacion o redireccion segun permisos
     */
    @GetMapping("/spaces/{spaceId}/reservas/nueva")
    public String showCreateReservation(
            @PathVariable Long spaceId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            HttpSession session,
            Model model
    ) {
        String accessRedirect = redirectIfNotAuthenticatedUser(session);
        if (accessRedirect != null) {
            return accessRedirect;
        }

        Optional<Space> space = findActiveSpace(spaceId);
        if (space.isEmpty()) {
            return "redirect:/spaces";
        }

        Integer recurrenceWeeks = 2;
        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;
        if (dateFrom != null) {
            LocalDateTime start = dateFrom.atTime(9, 0);
            startDateTime = start;
            endDateTime = start.plusHours(1);
        }

        model.addAttribute("space", space.get());
        populateReservationFormModel(model, startDateTime, endDateTime, null, false, recurrenceWeeks);
        return "reservation/create";
    }

    /**
     * Procesa la creacion de una reserva para un espacio.
     *
     * @param spaceId identificador del espacio
     * @param startDateTime fecha y hora de inicio
     * @param endDateTime fecha y hora de fin
     * @param reason motivo de la reserva
     * @param recurringWeekly indica si es recurrente semanal
     * @param recurrenceWeeks numero de semanas de recurrencia
     * @param session sesion HTTP
     * @param model modelo de vista
     * @return vista con errores o redireccion al detalle
     */
    @PostMapping("/spaces/{spaceId}/reservas/nueva")
    public String createReservation(
            @PathVariable Long spaceId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDateTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDateTime,
            @RequestParam(required = false) String reason,
            @RequestParam(defaultValue = "false") boolean recurringWeekly,
            @RequestParam(required = false) Integer recurrenceWeeks,
            HttpSession session,
            Model model
    ) {
        String accessRedirect = redirectIfNotAuthenticatedUser(session);
        if (accessRedirect != null) {
            return accessRedirect;
        }

        Optional<Space> space = findActiveSpace(spaceId);
        if (space.isEmpty()) {
            return "redirect:/spaces";
        }

        Errors errors = new MapBindingResult(new HashMap<>(), "reservation");
        reservationValidator.validate(startDateTime, endDateTime, reason, recurringWeekly, recurrenceWeeks, errors);
        if (errors.hasErrors()) {
            model.addAttribute("space", space.get());
            populateReservationFormModel(model, startDateTime, endDateTime, reason, recurringWeekly, recurrenceWeeks);
            populateReservationFormErrors(model, errors);
            return "reservation/create";
        }

        ReservationService.ReservationCreationResult creationResult = reservationService.createReservation(
                spaceId,
                (String) session.getAttribute("dni"),
                startDateTime,
                endDateTime,
                reason,
                recurringWeekly,
                recurringWeekly ? recurrenceWeeks : 1
        );

        if (!creationResult.isSuccess()) {
            model.addAttribute("space", space.get());
            model.addAttribute("formError", creationResult.getMessage());
            session.setAttribute("reservationCreationError", creationResult.getMessage());
            populateReservationFormModel(model, startDateTime, endDateTime, reason, recurringWeekly, recurrenceWeeks);
            return "reservation/create";
        }

        LocalDate redirectFrom = startDateTime.toLocalDate();
        LocalDate redirectTo = endDateTime.toLocalDate();
        String redirectUrl = UriComponentsBuilder.fromPath("/spaces/{id}")
                .queryParam("dateFrom", redirectFrom)
                .queryParam("dateTo", redirectTo)
                .buildAndExpand(spaceId)
                .toUriString();
        return "redirect:" + redirectUrl;
    }

    /**
     * Muestra el listado de reservas propias del usuario.
     *
     * @param status filtro por estado (opcional)
     * @param session sesion HTTP
     * @param model modelo de vista
     * @return vista de reservas propias o redireccion segun permisos
     */
    @GetMapping("/reservas/mis")
    public String myReservations(
            @RequestParam(required = false) String status,
            HttpSession session,
            Model model
    ) {
        if (!isAuthenticated(session)) {
            return "redirect:/login";
        }
        if (!isUser(session)) {
            return "redirect:/espacios/disponibles";
        }

        String dni = (String) session.getAttribute("dni");
        List<ReservationService.UserReservationRow> reservations = reservationService.findUserReservations(dni, status);
        String creationError = (String) session.getAttribute("reservationCreationError");
        if (creationError != null) {
            model.addAttribute("creationError", creationError);
            session.removeAttribute("reservationCreationError");
        }
        model.addAttribute("reservations", reservations);
        model.addAttribute("selectedStatus", status);
        return "reservation/my-list";
    }

    /**
     * Cancela una reserva del usuario autenticado.
     *
     * @param reservationId identificador de la reserva
     * @param session sesion HTTP
     * @param model modelo de vista
     * @return redireccion al listado o vista con error
     */
    @PostMapping("/reservas/{reservationId}/cancelar")
    public String cancelReservation(
            @PathVariable Long reservationId,
            HttpSession session,
            Model model
    ) {
        if (!isAuthenticated(session)) {
            return "redirect:/login";
        }
        if (!isUser(session)) {
            return "redirect:/espacios/disponibles";
        }

        String dni = (String) session.getAttribute("dni");
        ReservationService.ReservationActionResult result = reservationService.cancelReservation(reservationId, dni);
        if (!result.isSuccess()) {
            List<ReservationService.UserReservationRow> reservations = reservationService.findUserReservations(dni, null);
            model.addAttribute("reservations", reservations);
            model.addAttribute("cancelError", result.getMessage());
            return "reservation/my-list";
        }

        return "redirect:/reservas/mis";
    }

    /**
     * Muestra el listado global de reservas para administradores.
     *
     * @param page pagina solicitada
     * @param spaceId filtro por espacio (opcional)
     * @param dateFrom fecha inicio (opcional)
     * @param dateTo fecha fin (opcional)
     * @param session sesion HTTP
     * @param model modelo de vista
     * @return vista de listado global o redireccion segun permisos
     */
    @GetMapping("/reservas/listado-global")
    public String globalReservations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Long spaceId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            HttpSession session,
            Model model
    ) {
        if (!isAuthenticated(session)) {
            return "redirect:/login";
        }
        if (!"ADMIN".equals(session.getAttribute("role"))) {
            return "redirect:/espacios/disponibles";
        }

        int safePage = Math.max(page, 0);
        Page<ReservationService.ReservationRow> reservationsPage = reservationService
                .findGlobalReservations(spaceId, dateFrom, dateTo, safePage, PAGE_SIZE);
        List<Space> spaces = spaceService.findAllSpacesOrderByName();

        model.addAttribute("reservationsPage", reservationsPage);
        model.addAttribute("spaces", spaces);
        model.addAttribute("selectedSpaceId", spaceId);
        model.addAttribute("dateFrom", dateFrom);
        model.addAttribute("dateTo", dateTo);
        return "reservation/global-list";
    }

    /**
     * Exporta el listado global de reservas a CSV.
     *
     * @param spaceId filtro por espacio (opcional)
     * @param dateFrom fecha inicio (opcional)
     * @param dateTo fecha fin (opcional)
     * @param session sesion HTTP
     * @param response respuesta HTTP
     * @throws IOException si falla la escritura
     */
    @GetMapping("/reservas/listado-global/csv")
    public void exportGlobalReservationsCsv(
            @RequestParam(required = false) Long spaceId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            HttpSession session,
            HttpServletResponse response
    ) throws IOException {
        if (!isAuthenticated(session)) {
            response.sendRedirect("/login");
            return;
        }
        if (!"ADMIN".equals(session.getAttribute("role"))) {
            response.sendRedirect("/espacios/disponibles");
            return;
        }

        List<ReservationService.ReservationRow> rows = reservationService.findGlobalReservations(spaceId, dateFrom, dateTo);
        List<String[]> csvRows = new ArrayList<>();
        csvRows.add(new String[] { "Espacio", "Usuario", "Inicio fecha", "Inicio hora", "Fin fecha", "Fin hora", "Estado" });
        for (ReservationService.ReservationRow row : rows) {
            csvRows.add(new String[] {
                    row.spaceName(),
                    row.userLabel(),
                    CsvUtils.formatDate(row.startDateTime()),
                    CsvUtils.formatTime(row.startDateTime()),
                    CsvUtils.formatDate(row.endDateTime()),
                    CsvUtils.formatTime(row.endDateTime()),
                    row.status().name()
            });
        }

        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"reservas.csv\"");
        response.getWriter().write(CsvUtils.toCsv(csvRows));
        response.flushBuffer();
    }

    /**
     * Comprueba si la sesion esta autenticada.
     *
     * @param session sesion HTTP
     * @return true si esta autenticado
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isAuthenticated(HttpSession session) {
        Object authenticated = session.getAttribute("authenticated");
        return authenticated instanceof Boolean && (Boolean) authenticated;
    }

    /**
     * Comprueba si el usuario autenticado es de rol USER.
     *
     * @param session sesion HTTP
     * @return true si es usuario estandar
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isUser(HttpSession session) {
        return "USER".equals(session.getAttribute("role"));
    }

    private String redirectIfNotAuthenticatedUser(HttpSession session) {
        if (!isAuthenticated(session)) {
            return "redirect:/login";
        }
        if (!isUser(session)) {
            return "redirect:/espacios/disponibles";
        }
        return null;
    }

    private Optional<Space> findActiveSpace(Long spaceId) {
        return spaceService.findActiveById(spaceId);
    }

    private void populateReservationFormModel(
            Model model,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            String reason,
            boolean recurringWeekly,
            Integer recurrenceWeeks
    ) {
        model.addAttribute("startDateTime", formatDateTime(startDateTime));
        model.addAttribute("endDateTime", formatDateTime(endDateTime));
        model.addAttribute("reason", reason);
        model.addAttribute("recurringWeekly", recurringWeekly);
        model.addAttribute("recurrenceWeeks", recurrenceWeeks);
    }

    private String formatDateTime(LocalDateTime value) {
        if (value == null) {
            return null;
        }
        return value.format(DATETIME_LOCAL_FORMAT);
    }

    private void populateReservationFormErrors(Model model, Errors errors) {
        addError(model, errors, "startDateTime", "startDateTimeError");
        addError(model, errors, "endDateTime", "endDateTimeError");
        addError(model, errors, "reason", "reasonError");
        addError(model, errors, "recurrenceWeeks", "recurrenceWeeksError");
    }

    private void addError(Model model, Errors errors, String field, String attribute) {
        var error = errors.getFieldError(field);
        if (error != null) {
            model.addAttribute(attribute, error.getCode());
        }
    }
}
