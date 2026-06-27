package com.uniovi.sdi.gradeManager.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class Department {
    @Id
    @GeneratedValue
    private Long id;
    private String code;
    private String name;
    private String faculty;
    private String phone;
    private Integer professors;

    public Department(){}

    public Department(Long id, String code, String name, String faculty, String phone, Integer professors) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.faculty = faculty;
        this.phone = phone;
        this.professors = professors;
    }

    @Override
    public String toString() {
        return "Department{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", faculty='" + faculty + '\'' +
                ", phone='" + phone + '\'' +
                ", professors=" + professors +
                '}';
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFaculty() {
        return faculty;
    }

    public void setFaculty(String faculty) {
        this.faculty = faculty;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getProfessors() {
        return professors;
    }

    public void setProfessors(Integer professors) {
        this.professors = professors;
    }
}
