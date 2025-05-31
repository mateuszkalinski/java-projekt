package com.example.librarysystem.repository;

import com.example.librarysystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Ta metoda będzie kluczowa dla Spring Security do ładowania użytkownika po nazwie
    Optional<User> findByUsername(String username);
}