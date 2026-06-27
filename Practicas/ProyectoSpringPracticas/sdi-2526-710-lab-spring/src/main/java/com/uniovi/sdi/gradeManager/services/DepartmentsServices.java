package com.uniovi.sdi.gradeManager.services;

import com.uniovi.sdi.gradeManager.entities.Department;
import com.uniovi.sdi.gradeManager.repositories.DepartmentsRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Service
public class DepartmentsServices {

    @Autowired
    private DepartmentsRepository departmentsRepository;

    @PostConstruct
    public void init(){
        departmentsRepository.save(new Department(null,"code1", "name1", "faculty1", "phone1", 10));
        departmentsRepository.save(new Department(null, "code2", "name2", "faculty2", "phone2", 100));
        departmentsRepository.save(new Department(null, "code3", "name3", "faculty3", "phone3", 1000));
        departmentsRepository.save(new Department(null, "code4", "name4", "faculty4", "phone4", 10000));

    }

    public List<Department> getDepartments(){
        List<Department> departments = new ArrayList<>();
        departmentsRepository.findAll().forEach(departments::add);
        return departments;
    }

    public Department getDepartment(Long id){
        return departmentsRepository.findById(id).orElseThrow(() -> new RuntimeException("Departamento no encontrado"));
    }

    public Department getDepartmentByCode(String code){
        return departmentsRepository.findByCode(code);
    }

    public void addDepartment(Department department){
        departmentsRepository.save(department);
    }

    public void deteleDepartment(Long id){
        departmentsRepository.deleteById(id);
    }
}
