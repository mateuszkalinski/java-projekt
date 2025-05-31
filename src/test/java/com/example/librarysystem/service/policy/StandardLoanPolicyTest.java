package com.example.librarysystem.service.policy;

import com.example.librarysystem.entity.Book;
import com.example.librarysystem.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class StandardLoanPolicyTest {

    private StandardLoanPolicy standardLoanPolicy;
    private Book dummyBook;
    private User dummyUser;
    private LocalDate baseDate;

    @BeforeEach
    void setUp() {
        standardLoanPolicy = new StandardLoanPolicy();
        // Możemy użyć pustych obiektów, bo w StandardLoanPolicy nie ma logiki zależnej od Book/User
        dummyBook = new Book();
        dummyBook.setId(1L);
        dummyBook.setTitle("Dummy");
        dummyBook.setAuthor("Dummy Author");
        dummyBook.setIsbn("000-0000000000");

        dummyUser = new User();
        dummyUser.setId(1L);
        dummyUser.setUsername("dummyUser");
        dummyUser.setRole("ROLE_USER");

        baseDate = LocalDate.of(2025, 6, 1); // dowolna data bazowa
    }

    @Test
    @DisplayName("calculateDueDate: powinno zwrócić borrowDate + 14 dni")
    void testCalculateDueDate_is14DaysLater() {
        LocalDate expected = baseDate.plusDays(14);
        LocalDate actual = standardLoanPolicy.calculateDueDate(baseDate, dummyBook, dummyUser);

        assertEquals(expected, actual, "Dla StandardLoanPolicy termin zwrotu powinien być +14 dni od daty wypożyczenia");
    }

    @Test
    @DisplayName("calculateDueDate: różne dni miesiąca, nadal +14 dni")
    void testCalculateDueDate_variousDates() {
        LocalDate date1 = LocalDate.of(2025, 1, 20);
        LocalDate expected1 = date1.plusDays(14);
        assertEquals(expected1, standardLoanPolicy.calculateDueDate(date1, dummyBook, dummyUser));

        LocalDate date2 = LocalDate.of(2025, 12, 25);
        LocalDate expected2 = date2.plusDays(14);
        assertEquals(expected2, standardLoanPolicy.calculateDueDate(date2, dummyBook, dummyUser));
    }
}
