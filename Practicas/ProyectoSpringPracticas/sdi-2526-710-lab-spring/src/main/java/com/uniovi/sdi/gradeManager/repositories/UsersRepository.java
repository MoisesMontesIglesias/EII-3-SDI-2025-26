package com.uniovi.sdi.gradeManager.repositories;

import com.uniovi.sdi.gradeManager.entities.User;
import org.springframework.data.repository.CrudRepository;

public interface UsersRepository extends CrudRepository<User, Long> {
    User findByDni(String dni);
}
