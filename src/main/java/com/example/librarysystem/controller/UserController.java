package com.example.librarysystem.controller;

import com.example.librarysystem.entity.User; // Zmieniony import, jeśli zmieniłeś nazwę encji na User.java
import com.example.librarysystem.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
// import java.util.stream.Collectors; // Może być potrzebne do DTO

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Endpoints for managing users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Get all users", description = "Retrieve a list of all users. (Admin only - to be secured)")
    @GetMapping
    // @PreAuthorize("hasRole('ADMIN')")
    public List<User> getAllUsers() {
        // UWAGA: Domyślnie to zwróci również zahaszowane hasła.
        // W prawdziwej aplikacji powinniśmy użyć DTO (Data Transfer Object)
        // aby nie eksponować haseł i innych wrażliwych danych.
        // Na razie dla uproszczenia zostawiamy, ale to WAŻNY punkt do poprawy.
        return userService.getAllUsers();
    }

    @Operation(summary = "Get user by ID", description = "Retrieve a specific user by their ID. (Admin or self - to be secured)")
    @GetMapping("/{id}")
    // @PreAuthorize("hasRole('ADMIN') or @userService.isSelf(authentication, #id)") // Przykład zabezpieczenia
    public ResponseEntity<User> getUserById(
            @Parameter(description = "ID of the user to retrieve", required = true, example = "1")
            @PathVariable Long id) {
        // Podobnie jak wyżej - uwaga na zwracanie hasła.
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Create a new user (Register)", description = "Create a new user with provided details. Password and role are required.")
    @PostMapping("/register") // Zmieniamy endpoint na bardziej opisowy dla rejestracji, lub zostawiamy /api/users
    public ResponseEntity<?> createUser( // Zmieniono User na ResponseEntity<?> dla lepszej obsługi błędów
                                         @Parameter(description = "User object to create. Username, password, and role are required.", required = true)
                                         @RequestBody User user) {
        try {
            // Na tym etapie rola może być ustawiana domyślnie na ROLE_USER,
            // lub przekazywana w żądaniu (co wymagałoby walidacji, czy użytkownik może nadać daną rolę).
            // Dla uproszczenia, załóżmy, że rola jest przekazywana w żądaniu.
            // userService.createUser będzie musiało obsłużyć hashowanie hasła (gdy dodamy Spring Security).
            if (user.getRole() == null || user.getRole().isEmpty()) {
                user.setRole("ROLE_USER"); // Domyślna rola, jeśli nie podano
            }
            User createdUser = userService.createUser(user);
            // Znowu, nie zwracajmy całego obiektu User z hasłem.
            // Tutaj można by zwrócić np. tylko ID i username, albo po prostu status 201.
            // Dla uproszczenia zwracamy na razie obiekt, ale to do zmiany.
            return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage()); // Np. "Username ... is already taken."
        }
    }

    @Operation(summary = "Update an existing user", description = "Update an existing user by their ID. (Admin or self - to be secured)")
    @PutMapping("/{id}")
    // @PreAuthorize("hasRole('ADMIN') or @userService.isSelf(authentication, #id)")
    public ResponseEntity<?> updateUser( // Zmieniono User na ResponseEntity<?>
                                         @Parameter(description = "ID of the user to update", required = true, example = "1")
                                         @PathVariable Long id,
                                         @Parameter(description = "User object with updated information. Fields to update can be partial.", required = true)
                                         @RequestBody User userDetails) {
        try {
            // userService.updateUser powinien obsłużyć logikę aktualizacji,
            // w tym np. niezerowanie pól, które nie są podane w userDetails,
            // oraz hashowanie hasła, jeśli jest zmieniane.
            User updatedUser = userService.updateUser(id, userDetails);
            // Nie zwracaj hasła!
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) { // Np. User not found lub username taken
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Delete a user", description = "Delete a user by their ID. (Admin only - to be secured)")
    @DeleteMapping("/{id}")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID of the user to delete", required = true, example = "1")
            @PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}