package com.example.librarysystem.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Set;

@Entity
@Table(name = "library_users")
@Getter
@Setter
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true) // Nazwa użytkownika powinna być unikalna i niepusta
    private String username;

    @Column(nullable = false) // Hasło nie powinno być puste
    private String password; // Dodajemy pole na hasło

    @Column(nullable = false) // Rola nie powinna być pusta
    private String role; // Np. "ROLE_USER", "ROLE_ADMIN"
}