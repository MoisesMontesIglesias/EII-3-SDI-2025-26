package com.uniovi.sdi.reservationmanagement.controllers;

import com.uniovi.sdi.reservationmanagement.entities.MaintenanceBlock;
import com.uniovi.sdi.reservationmanagement.entities.Space;
import com.uniovi.sdi.reservationmanagement.services.SpaceDetailService;
import com.uniovi.sdi.reservationmanagement.services.SpaceService;
import com.uniovi.sdi.reservationmanagement.validators.AddSpaceValidator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDate;
import java.util.Optional;
@Controller
public class SpaceController {

    private static final int PAGE_SIZE = 5;
    private final SpaceService spaceService;
    private final SpaceDetailService spaceDetailService;
    private final AddSpaceValidator addSpaceValidator;

    /**
     * Crea el controlador de espacios con sus dependencias.
     *
     * @param spaceService servicio de espacios
     * @param spaceDetailService servicio de detalle de espacios
     * @param addSpaceValidator validador de alta/edicion
     */
    public SpaceController(
            SpaceService spaceService,
            SpaceDetailService spaceDetailService,
            AddSpaceValidator addSpaceValidator
    ) {
        this.spaceService = spaceService;
        this.spaceDetailService = spaceDetailService;
        this.addSpaceValidator = addSpaceValidator;
    }

    /**
     * Lista espacios disponibles o todos segun el rol, con filtros y paginacion.
     *
     * @param page numero de pagina
     * @param type tipo de espacio (opcional)
     * @param minCapacity capacidad minima (opcional)
     * @param model modelo de vista
     * @return vista del listado de espacios
     */
    @GetMapping({"/spaces", "/espacios/disponibles"})
    public String listAvailableSpaces(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer minCapacity,
            Model model
    ) {
        int safePage = Math.max(page, 0);
        var spacesPage = isAdmin()
                ? spaceService.findAllSpacesPage(type, minCapacity, safePage, PAGE_SIZE)
                : spaceService.findAvailableSpacesPage(type, minCapacity, safePage, PAGE_SIZE);
        model.addAttribute("spacesPage", spacesPage);
        model.addAttribute("spaces", spacesPage.getContent());
        model.addAttribute("selectedType", type == null ? "" : type.trim());
        model.addAttribute("selectedMinCapacity", minCapacity == null ? "" : minCapacity);
        model.addAttribute("spaceTypes", java.util.List.of("SALA", "AULA", "COWORK"));
        return "space/list";
    }

    /**
     * Muestra el formulario de alta de espacio.
     *
     * @param model modelo de vista
     * @return vista de alta de espacio
     */
    @GetMapping("/espacios/nuevo")
    public String showCreateForm(Model model) {
        model.addAttribute("space", new Space());
        model.addAttribute("spaceTypes", java.util.List.of("SALA", "AULA", "COWORK"));
        return "space/add";
    }

    /**
     * Procesa el alta de un nuevo espacio.
     *
     * @param space datos del formulario
     * @param result resultado de validacion
     * @param model modelo de vista
     * @return redireccion al listado o vista con errores
     */
    @PostMapping("/espacios/nuevo")
    public String createSpace(
            @ModelAttribute("space") Space space,
            BindingResult result,
            Model model
    ) {
        addSpaceValidator.validate(space, result);

        if (result.hasErrors()) {
            model.addAttribute("space", space);
            model.addAttribute("spaceTypes", java.util.List.of("SALA", "AULA", "COWORK"));
            return "space/add";
        }

        spaceService.createSpace(
                space.getName(),
                space.getType(),
                space.getLocation(),
                space.getCapacity(),
                space.getStatus(),
                space.getDescription()
        );
        return "redirect:/spaces";
    }

    /**
     * Muestra el formulario de edicion de un espacio existente.
     *
     * @param spaceId identificador del espacio
     * @param model modelo de vista
     * @return vista de edicion o redireccion si no existe
     */
    @GetMapping("/espacios/{spaceId}/editar")
    public String showEditForm(@PathVariable Long spaceId, Model model) {
        Optional<Space> space = spaceService.findById(spaceId);
        if (space.isEmpty()) {
            return "redirect:/spaces";
        }
        model.addAttribute("space", space.get());
        return "space/edit";
    }

    /**
     * Procesa la edicion de un espacio.
     *
     * @param spaceId identificador del espacio
     * @param space datos del formulario
     * @param result resultado de validacion
     * @param model modelo de vista
     * @return redireccion al detalle o vista con errores
     */
    @PostMapping("/espacios/{spaceId}/editar")
    public String updateSpace(
            @PathVariable Long spaceId,
            @ModelAttribute("space") Space space,
            BindingResult result,
            Model model
    ) {
        addSpaceValidator.validate(space, result);
        if (result.hasErrors()) {
            model.addAttribute("space", space);
            return "space/edit";
        }

        Optional<Space> original = spaceService.findById(spaceId);
        if (original.isEmpty()) {
            return "redirect:/spaces";
        }
        Space originalSpace = original.get();
        originalSpace.setName(space.getName());
        originalSpace.setType(space.getType());
        originalSpace.setLocation(space.getLocation());
        originalSpace.setCapacity(space.getCapacity());
        originalSpace.setDescription(space.getDescription());
        originalSpace.setStatus(space.getStatus());
        spaceService.saveSpace(originalSpace);
        return "redirect:/spaces/" + spaceId;
    }

    /**
     * Muestra el detalle de un espacio con disponibilidad.
     *
     * @param spaceId identificador del espacio
     * @param dateFrom fecha inicio (opcional)
     * @param dateTo fecha fin (opcional)
     * @param model modelo de vista
     * @return vista de detalle o redireccion si no existe
     */
    @GetMapping("/spaces/{spaceId}")
    public String spaceDetail(
            @PathVariable Long spaceId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            Model model
    ) {
        return renderSpaceDetail(spaceId, dateFrom, dateTo, model);
    }

    /**
     * Consulta disponibilidad de un espacio en un rango de fechas.
     *
     * @param spaceId identificador del espacio
     * @param dateFrom fecha inicio
     * @param dateTo fecha fin
     * @param model modelo de vista
     * @return vista de detalle con franjas ocupadas
     */
    @GetMapping("/spaces/{spaceId}/availability")
    public String spaceAvailability(
            @PathVariable Long spaceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            Model model
    ) {
        return renderSpaceDetail(spaceId, dateFrom, dateTo, model);
    }

    private String renderSpaceDetail(Long spaceId, LocalDate dateFrom, LocalDate dateTo, Model model) {
        Optional<SpaceDetailService.SpaceDetailResult> detail = spaceDetailService.buildDetail(
                spaceId,
                dateFrom,
                dateTo,
                isAdmin()
        );
        if (detail.isEmpty()) {
            return "redirect:/spaces";
        }
        if (!model.asMap().containsKey("blockForm")) {
            model.addAttribute("blockForm", new MaintenanceBlock());
        }
        model.addAttribute("showBlockModal", model.asMap().getOrDefault("showBlockModal", false));
        model.addAttribute("space", detail.get().space());
        model.addAttribute("dateFrom", detail.get().dateFrom());
        model.addAttribute("dateTo", detail.get().dateTo());
        model.addAttribute("occupiedSlots", detail.get().occupiedSlots());
        model.addAttribute("maintenanceBlocks", detail.get().maintenanceBlocks());
        return "space/detail";
    }

    /**
     * Determina si el usuario actual es administrador.
     *
     * @return true si es admin
     */
    private boolean isAdmin() {
        var authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication();
        if (authentication != null
                && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()))) {
            return true;
        }
        var attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletAttributes) {
            var session = servletAttributes.getRequest().getSession(false);
            if (session != null) {
                Object role = session.getAttribute("role");
                return "ADMIN".equals(role);
            }
        }
        return false;
    }

}
