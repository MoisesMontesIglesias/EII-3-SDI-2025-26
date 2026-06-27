package com.uniovi.sdi.reservationmanagement.repositories;

import com.uniovi.sdi.reservationmanagement.entities.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {
    Optional<User> findByDni(String dni);
    boolean existsByDni(String dni);
}
