package com.example.librarysystem.service;

import com.example.librarysystem.entity.User;
import com.example.librarysystem.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority; // <--- NOWY IMPORT
import org.springframework.security.core.authority.SimpleGrantedAuthority; // <--- NOWY IMPORT
import org.springframework.security.core.userdetails.UserDetails; // <--- NOWY IMPORT
import org.springframework.security.core.userdetails.UserDetailsService; // <--- NOWY IMPORT
import org.springframework.security.core.userdetails.UsernameNotFoundException; // <--- NOWY IMPORT
import org.springframework.security.crypto.password.PasswordEncoder; // <--- NOWY IMPORT
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection; // <--- NOWY IMPORT
import java.util.Collections; // <--- NOWY IMPORT
import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService { // <--- IMPLEMENTUJEMY UserDetailsService

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // <--- WSTRZYKUJEMY PasswordEncoder

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) { // <--- Aktualizacja konstruktora
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Metoda z interfejsu UserDetailsService
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // Konwersja naszej roli (np. "ROLE_ADMIN") na kolekcję GrantedAuthority
        // Spring Security oczekuje ról w formacie "ROLE_NAZWAROLI"
        GrantedAuthority authority = new SimpleGrantedAuthority(user.getRole());
        Collection<GrantedAuthority> authorities = Collections.singletonList(authority);

        // Używamy wbudowanej implementacji UserDetails od Spring Security
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(), // Hasło pobrane z bazy (powinno być już zahashowane)
                authorities // Role/uprawnienia użytkownika
        );
        // Można też stworzyć własną klasę implementującą UserDetails, jeśli potrzebujemy więcej pól
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional
    public User createUser(User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new IllegalStateException("Username " + user.getUsername() + " is already taken.");
        }
        // Hashowanie hasła przed zapisem!
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // Domyślna rola, jeśli nie podano (można też przenieść do kontrolera lub walidacji)
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("ROLE_USER"); // Upewnij się, że role zaczynają się od "ROLE_"
        } else if (!user.getRole().startsWith("ROLE_")) {
            user.setRole("ROLE_" + user.getRole().toUpperCase()); // Normalizacja nazwy roli
        }

        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(Long id, User userDetails) {
        User userToUpdate = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        if (userDetails.getUsername() != null && !userDetails.getUsername().equals(userToUpdate.getUsername())) {
            if (userRepository.findByUsername(userDetails.getUsername()).isPresent()) {
                throw new IllegalStateException("Username " + userDetails.getUsername() + " is already taken.");
            }
            userToUpdate.setUsername(userDetails.getUsername());
        }

        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            // Hashowanie nowego hasła, jeśli zostało zmienione
            userToUpdate.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }

        if (userDetails.getRole() != null && !userDetails.getRole().isEmpty()) {
            String role = userDetails.getRole();
            if (!role.startsWith("ROLE_")) {
                role = "ROLE_" + role.toUpperCase();
            }
            userToUpdate.setRole(role);
        }

        return userRepository.save(userToUpdate);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }
}