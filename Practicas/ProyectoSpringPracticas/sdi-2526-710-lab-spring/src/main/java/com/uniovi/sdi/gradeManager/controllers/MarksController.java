package com.uniovi.sdi.gradeManager.controllers;

import com.uniovi.sdi.gradeManager.entities.Mark;
import com.uniovi.sdi.gradeManager.entities.User;
import com.uniovi.sdi.gradeManager.services.MarksService;
import com.uniovi.sdi.gradeManager.services.UsersService;
import com.uniovi.sdi.gradeManager.validators.AddMarkFormValidator;
import com.uniovi.sdi.gradeManager.validators.SignUpFormValidator;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

@Controller
public class MarksController {

    private final MarksService marksService;
    private final UsersService usersService;
    private final AddMarkFormValidator addMarkFormValidator;

    public MarksController(MarksService marksService, UsersService usersService, AddMarkFormValidator addMarkFormValidator) {
        this.marksService = marksService;
        this.usersService = usersService;
        this.addMarkFormValidator = addMarkFormValidator;
    }

    @GetMapping(value = "/mark/add")
    public String getMark(Model model) {
        model.addAttribute("marksList", usersService.getUsers());
        model.addAttribute("mark", new Mark());
        return "mark/add";
    }

    @PostMapping("/mark/add")
    public String setMark(@Validated @ModelAttribute Mark mark, BindingResult result, Model model) {
        addMarkFormValidator.validate(mark, result);
        if (result.hasErrors()) {
            model.addAttribute("usersList", usersService.getUsers());
            return "mark/add";
        }
        marksService.addMark(mark);
        return "redirect:/mark/list";
    }

    @GetMapping("/mark/list")
    public String getList(Model model, Pageable pageable, Principal principal, @RequestParam(value = "",
            required = false) String searchText) {
        String dni = principal.getName();
        User user = usersService.getUserByDni(dni);
        Page<Mark> marks;
        if (searchText != null && !searchText.isEmpty()) {
            marks = marksService.searchMarksByDescriptionAndNameForUser(pageable, searchText, user);
        } else {
            marks = marksService.getMarksForUser(pageable, user);
        }
        model.addAttribute("marksList", marks.getContent());
        model.addAttribute("page", marks);
        return "mark/list";
    }


    @GetMapping("/mark/details/{id}")
    public String getDetail(Model model, @PathVariable Long id) {
        model.addAttribute("mark", marksService.getMark(id));
        return "mark/details";
    }

    @RequestMapping("/mark/delete/{id}")
    public String deleteMark(@PathVariable Long id) {
        marksService.deleteMark(id);
        return "redirect:/mark/list";
    }

    @GetMapping(value = "/mark/edit/{id}")
    public String getEdit(Model model, @PathVariable Long id) {
        model.addAttribute("mark", marksService.getMark(id));
        model.addAttribute("usersList", usersService.getUsers());
        return "mark/edit";
    }

    @PostMapping(value="/mark/edit/{id}")
    public String setEdit(@ModelAttribute Mark mark, @PathVariable Long id){
        Mark originalMark = marksService.getMark(id);
        originalMark.setScore(mark.getScore());
        originalMark.setDescription(mark.getDescription());
        marksService.addMark(originalMark);
        return "redirect:/mark/details/"+id;
    }

    @GetMapping("/mark/list/update")
    public String updateList(Model model, Pageable pageable, Principal principal) {
        String dni = principal.getName(); // DNI es el name de la autenticación
        User user = usersService.getUserByDni(dni);
        Page<Mark> marks = marksService.getMarksForUser(pageable, user);
        model.addAttribute("marksList", marks.getContent());
        return "mark/list :: marksTable";
    }

    @GetMapping("/mark/{id}/resend")
    public String setResendTrue(@PathVariable Long id) {
        marksService.setMarkResend(true, id);
        return "redirect:/mark/list";
    }
    @GetMapping("/mark/{id}/noresend")
    public String setResendFalse(@PathVariable Long id) {
        marksService.setMarkResend(false, id);
        return "redirect:/mark/list";
    }
}
