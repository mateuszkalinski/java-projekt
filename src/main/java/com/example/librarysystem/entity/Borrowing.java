package com.example.librarysystem.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Encja reprezentująca wypożyczenie książki przez użytkownika.
 * Dodajemy adnotacje @JsonIgnoreProperties, aby Jackson nie próbował serializować
 * wewnętrznych proxy Hibernate (ByteBuddyInterceptor) ani powodować cykli.
 */
@Entity
@Table(name = "borrowings")
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Borrowing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Relacja do User. FetchType.LAZY → proxy → dlatego dodajemy @JsonIgnoreProperties,
     * by Jackson nie próbował serializować proxy Hibernate i nie tworzył cykli.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({ "borrowings", "hibernateLazyInitializer", "handler" })
    private User user;

    /**
     * Relacja do Book. FetchType.LAZY → proxy → dlatego dodajemy @JsonIgnoreProperties,
     * by Jackson nie próbował serializować proxy Hibernate i nie tworzył cykli.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    @JsonIgnoreProperties({ "borrowings", "hibernateLazyInitializer", "handler" })
    private Book book;

    @Column(nullable = false)
    private LocalDate borrowDate; // Data wypożyczenia

    @Column(nullable = false)
    private LocalDate dueDate; // Data, do której książka powinna być zwrócona

    @Column
    private LocalDate returnDate; // Data faktycznego zwrotu (może być null, jeśli nie zwrócono)

    public Borrowing(User user, Book book, LocalDate borrowDate, LocalDate dueDate) {
        this.user = user;
        this.book = book;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.returnDate = null;
    }
}
