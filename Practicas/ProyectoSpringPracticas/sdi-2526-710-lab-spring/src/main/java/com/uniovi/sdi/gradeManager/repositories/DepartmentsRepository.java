package com.uniovi.sdi.gradeManager.repositories;

import com.uniovi.sdi.gradeManager.entities.Department;
import com.uniovi.sdi.gradeManager.entities.Mark;
import org.springframework.data.repository.CrudRepository;

public interface DepartmentsRepository extends CrudRepository<Department, Long> {
    Department findByCode(String code);
}