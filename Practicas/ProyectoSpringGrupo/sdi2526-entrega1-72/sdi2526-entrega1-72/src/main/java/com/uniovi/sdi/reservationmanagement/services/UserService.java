package com.uniovi.sdi.reservationmanagement.services;

import com.uniovi.sdi.reservationmanagement.entities.User;
import com.uniovi.sdi.reservationmanagement.repositories.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public User registerStandardUser(User user) {
        user.setRole("USER");
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean authenticate(String dni, String password) {
        Optional<User> user = userRepository.findByDni(dni);
        return user.isPresent() && bCryptPasswordEncoder.matches(password, user.get().getPassword());
    }

    public Optional<User> findByDni(String dni) {
        return userRepository.findByDni(dni);
    }

    public boolean existsByDni(String dni) {
        return userRepository.existsByDni(dni);
    }

    public void updatePassword(String dni, String newPassword) {
        User user = userRepository.findByDni(dni).orElseThrow();
        user.setPassword(bCryptPasswordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public void registerAdminIfAbsent(User admin) {
        if (userRepository.existsByDni(admin.getDni())) {
            return;
        }
        admin.setRole("ADMIN");
        admin.setPassword(bCryptPasswordEncoder.encode(admin.getPassword()));
        userRepository.save(admin);
    }
}
