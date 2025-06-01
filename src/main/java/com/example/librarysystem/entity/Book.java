package com.example.librarysystem.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Set;
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) // Tytuł, wymagany
    private String title;

    @Column(nullable = false) // Autor, wymagany
    private String author;

    @Column(unique = true) // ISBN, unikalny
    private String isbn;

    private String publisher; // Wydawca (opcjonalnie)

    private Integer publicationYear; // Rok wydania (opcjonalnie)

    private String genre; // Gatunek (opcjonalnie)

}