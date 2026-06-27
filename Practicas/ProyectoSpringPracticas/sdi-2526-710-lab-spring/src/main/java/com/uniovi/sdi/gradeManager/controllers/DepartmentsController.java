package com.uniovi.sdi.gradeManager.controllers;

import com.uniovi.sdi.gradeManager.entities.Department;
import com.uniovi.sdi.gradeManager.services.DepartmentsServices;
import com.uniovi.sdi.gradeManager.validators.AddDepartmentFormValidator;
import com.uniovi.sdi.gradeManager.validators.SignUpFormValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Controller
public class DepartmentsController {

    private final DepartmentsServices departmentServices;
    private final AddDepartmentFormValidator addDepartmentFormValidator;

    public DepartmentsController(DepartmentsServices departmentServices,
                                 AddDepartmentFormValidator addDepartmentFormValidator) {
        this.departmentServices = departmentServices;
        this.addDepartmentFormValidator = addDepartmentFormValidator;
    }

    @GetMapping("/departments/add")
    public String getDepartment(Model model) {
        model.addAttribute("department", new Department());
        return "departments/add";
    }

    @PostMapping("/departments/add")
    public String setDepartment(@Validated @ModelAttribute Department department, BindingResult result) {
        addDepartmentFormValidator.validate(department, result);
        if (result.hasErrors()) {
            return "departments/add";
        }
        departmentServices.addDepartment(department);
        return "redirect:/departments";
    }

    @GetMapping("/departments")
    public String getDepartments(Model departmentModel) {
        departmentModel.addAttribute("departmentList", departmentServices.getDepartments());
        return "departments/list";
    }

    @GetMapping("/departments/details/{id}")
    public String getProfessorDetail(@PathVariable Long id, Model departmentModel){
        departmentModel.addAttribute("department", departmentServices.getDepartment(id));
        return "departments/details";
    }

    @RequestMapping("/departments/delete/{id}")
    public String deleteDepartment(@PathVariable Long id) {
        departmentServices.deteleDepartment(id);
        return "redirect:/departments";
    }

    @GetMapping(value = "/departments/edit/{id}")
    public String getEdit(@PathVariable Long id, Model model) {
        model.addAttribute("department", departmentServices.getDepartment(id));
        return "departments/edit";
    }

    @PostMapping("/departments/edit/{id}")
    public String setEdit(@ModelAttribute Department department, @PathVariable Long id){
        department.setId(id);
        departmentServices.addDepartment(department);
        return "redirect:/departments/details/" + id;
    }
}
