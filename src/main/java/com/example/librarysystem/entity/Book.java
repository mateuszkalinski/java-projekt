package com.example.librarysystem.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) // Tytuł nie powinien być pusty
    private String title;

    @Column(nullable = false) // Autor też raczej wymagany
    private String author;

    @Column(unique = true) // ISBN powinien być unikalny
    private String isbn;

    private String publisher; // Wydawca (opcjonalnie)

    private Integer publicationYear; // Rok wydania (opcjonalnie)

    private String genre; // Gatunek (opcjonalnie)

}