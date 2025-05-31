package com.example.librarysystem.service.policy;

import com.example.librarysystem.entity.Book;
import com.example.librarysystem.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class AcademicLoanPolicyTest {

    private AcademicLoanPolicy academicLoanPolicy;
    private Book dummyBook;
    private User dummyUser;
    private LocalDate baseDate;

    @BeforeEach
    void setUp() {
        academicLoanPolicy = new AcademicLoanPolicy();

        dummyBook = new Book();
        dummyBook.setId(1L);
        dummyBook.setTitle("Academic Book");
        dummyBook.setAuthor("Some Author");
        dummyBook.setIsbn("111-1111111111");
        // dokładnie nie ustawiamy na razie gatunku – sprawdzimy dwa scenariusze w testach

        dummyUser = new User();
        dummyUser.setId(1L);
        dummyUser.setUsername("studentUser");
        dummyUser.setRole("ROLE_USER");

        baseDate = LocalDate.of(2025, 6, 1);
    }

    @Test
    @DisplayName("calculateDueDate: gdy gatunek = BESTSELLER (wielkość dowolna) powinno być +7 dni")
    void testCalculateDueDate_whenGenreIsBestseller() {
        dummyBook.setGenre("BEStSELLER"); // sprawdzamy ignorowanie wielkości liter
        LocalDate expected = baseDate.plusDays(7);
        LocalDate actual = academicLoanPolicy.calculateDueDate(baseDate, dummyBook, dummyUser);

        assertEquals(expected, actual, "Dla książek z gatunku 'BESTSELLER' termin to +7 dni");
    }

    @Test
    @DisplayName("calculateDueDate: gdy gatunek nie jest BESTSELLER, powinno być +30 dni")
    void testCalculateDueDate_whenGenreIsNotBestseller() {
        dummyBook.setGenre("Science"); // jakiś inny gatunek
        LocalDate expected = baseDate.plusDays(30);
        LocalDate actual = academicLoanPolicy.calculateDueDate(baseDate, dummyBook, dummyUser);

        assertEquals(expected, actual, "Dla innych książek w polityce akademickiej termin to +30 dni");
    }

    @Test
    @DisplayName("calculateDueDate: gdy gatunek jest null (brak ustawionego gatunku), powinno być +30 dni")
    void testCalculateDueDate_whenGenreIsNull() {
        dummyBook.setGenre(null); // brak gatunku
        LocalDate expected = baseDate.plusDays(30);
        LocalDate actual = academicLoanPolicy.calculateDueDate(baseDate, dummyBook, dummyUser);

        assertEquals(expected, actual, "Brak gatunku traktujemy jak nie-bestseller → +30 dni");
    }
}
