package com.example.librarysystem.service.policy;

import com.example.librarysystem.entity.Book;
import com.example.librarysystem.entity.User;
import java.time.LocalDate;

public interface LoanPolicy {
    /**
     * Oblicza termin zwrotu książki na podstawie daty wypożyczenia,
     * informacji o książce i użytkowniku.
     *
     * @param borrowDate Data wypożyczenia.
     * @param book       Wypożyczana książka.
     * @param user       Użytkownik wypożyczający książkę.
     * @return Obliczona data zwrotu.
     */
    LocalDate calculateDueDate(LocalDate borrowDate, Book book, User user);

    // W przyszłości można dodać inne metody, np. dotyczące kar za przetrzymanie:
    // BigDecimal calculateFine(LocalDate dueDate, LocalDate returnDate, Book book);
}