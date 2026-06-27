package com.uniovi.sdi.reservationmanagement.controllers;

import com.uniovi.sdi.reservationmanagement.entities.MaintenanceBlock;
import com.uniovi.sdi.reservationmanagement.entities.Space;
import com.uniovi.sdi.reservationmanagement.services.MaintenanceBlockService;
import com.uniovi.sdi.reservationmanagement.services.SpaceDetailService;
import com.uniovi.sdi.reservationmanagement.services.SpaceService;
import com.uniovi.sdi.reservationmanagement.validators.MaintenanceBlockValidator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Optional;

@Controller
public class MaintenanceBlockController {

    private final MaintenanceBlockService maintenanceBlockService;
    private final SpaceService spaceService;
    private final SpaceDetailService spaceDetailService;
    private final MaintenanceBlockValidator maintenanceBlockValidator;

    /**
     * Crea el controlador de bloqueos de mantenimiento.
     *
     * @param maintenanceBlockService servicio de bloqueos
     * @param spaceService servicio de espacios
     * @param spaceDetailService servicio de detalle de espacios
     * @param maintenanceBlockValidator validador de bloqueos
     */
    public MaintenanceBlockController(
            MaintenanceBlockService maintenanceBlockService,
            SpaceService spaceService,
            SpaceDetailService spaceDetailService,
            MaintenanceBlockValidator maintenanceBlockValidator
    ) {
        this.maintenanceBlockService = maintenanceBlockService;
        this.spaceService = spaceService;
        this.spaceDetailService = spaceDetailService;
        this.maintenanceBlockValidator = maintenanceBlockValidator;
    }

    /**
     * Registra un bloqueo de mantenimiento para un espacio.
     *
     * @param spaceId identificador del espacio
     * @param blockForm formulario de bloqueo
     * @param result resultado de validacion
     * @param model modelo de vista
     * @return redireccion al detalle o vista con errores
     */
    @PostMapping("/espacios/{spaceId}/bloqueos/nuevo")
    public String createBlock(
            @PathVariable Long spaceId,
            @ModelAttribute("blockForm") MaintenanceBlock blockForm,
            BindingResult result,
            Model model
    ) {
        Optional<Space> space = spaceService.findById(spaceId);
        if (space.isEmpty()) {
            return "redirect:/spaces";
        }

        maintenanceBlockValidator.validate(blockForm, result, spaceId);
        if (result.hasErrors()) {
            var detail = spaceDetailService.buildDetailForCurrentUser(spaceId, null, null);
            if (detail.isEmpty()) {
                return "redirect:/spaces";
            }
            addSpaceDetailAttributes(model, detail.get());
            if (blockForm.getStartDateTime() != null && blockForm.getEndDateTime() != null) {
                model.addAttribute(
                        "collisionSlots",
                        maintenanceBlockValidator.findCollisionSlots(
                                spaceId,
                                blockForm.getStartDateTime(),
                                blockForm.getEndDateTime()
                        )
                );
            }
            model.addAttribute("blockForm", blockForm);
            model.addAttribute("showBlockModal", true);
            return "space/detail";
        }

        maintenanceBlockService.createBlock(
                space.get(),
                blockForm.getStartDateTime(),
                blockForm.getEndDateTime(),
                blockForm.getReason()
        );
        return "redirect:/spaces/" + spaceId;
    }

    /**
     * Cancela un bloqueo de mantenimiento activo.
     *
     * @param spaceId identificador del espacio
     * @param blockId identificador del bloqueo
     * @return redireccion al detalle del espacio
     */
    @PostMapping("/espacios/{spaceId}/bloqueos/{blockId}/cancelar")
    public String cancelBlock(@PathVariable Long spaceId, @PathVariable Long blockId) {
        maintenanceBlockService.cancelBlock(spaceId, blockId);
        return "redirect:/spaces/" + spaceId;
    }

    private void addSpaceDetailAttributes(Model model, SpaceDetailService.SpaceDetailResult detail) {
        model.addAttribute("space", detail.space());
        model.addAttribute("dateFrom", detail.dateFrom());
        model.addAttribute("dateTo", detail.dateTo());
        model.addAttribute("occupiedSlots", detail.occupiedSlots());
        model.addAttribute("maintenanceBlocks", detail.maintenanceBlocks());
    }
}
