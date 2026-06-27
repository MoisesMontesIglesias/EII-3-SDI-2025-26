package com.uniovi.sdi.gradeManager.validators;

import com.uniovi.sdi.gradeManager.entities.Department;
import com.uniovi.sdi.gradeManager.services.DepartmentsServices;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
public class AddDepartmentFormValidator implements Validator {
    private final DepartmentsServices departmentsServices;

    public AddDepartmentFormValidator(DepartmentsServices departmentsServices) {
        this.departmentsServices = departmentsServices;
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return Department.class.equals(aClass);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Department department = (Department) target;
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "code", "Error.empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "Error.add.name.empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "faculty", "Error.add.faculty.empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "phone", "Error.add.phone.empty");

        String code = department.getCode();
        if (code != null && !code.isBlank()) {
            if (code.length() != 9 || !Character.isLetter(code.charAt(8))) {
                errors.rejectValue("code", "Error.add.code.length");
            }
            Department existingDepartment = departmentsServices.getDepartmentByCode(code);
            if (departmentsServices.getDepartment(department.getId()) != null &&
                    (department.getId() == null || !departmentsServices.getDepartmentByCode(code).getId().equals(department.getId()))) {
                errors.rejectValue("code", "Error.add.code.duplicate");
            }
        }
    }
}
