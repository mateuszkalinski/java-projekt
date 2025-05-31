package com.example.librarysystem.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate; // Użyjemy LocalDate dla dat

@Entity
@Table(name = "borrowings")
@Getter
@Setter
@NoArgsConstructor
public class Borrowing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // Leniwe ładowanie jest często dobrym wyborem
    @JoinColumn(name = "user_id", nullable = false) // Klucz obcy do tabeli użytkowników
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false) // Klucz obcy do tabeli książek
    private Book book;

    @Column(nullable = false)
    private LocalDate borrowDate; // Data wypożyczenia

    @Column(nullable = false)
    private LocalDate dueDate; // Data, do której książka powinna być zwrócona

    private LocalDate returnDate; // Data faktycznego zwrotu (może być null, jeśli nie zwrócono)

    // Konstruktor może być przydatny
    public Borrowing(User user, Book book, LocalDate borrowDate, LocalDate dueDate) {
        this.user = user;
        this.book = book;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.returnDate = null; // Przy nowym wypożyczeniu data zwrotu jest pusta
    }

    // Lombok wygeneruje gettery i settery
    // Możemy dodać metodę pomocniczą, np. do oznaczania zwrotu:
    // public boolean isReturned() {
    //     return returnDate != null;
    // }
}