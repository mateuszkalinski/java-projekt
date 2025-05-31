package com.example.librarysystem.service.policy;

import com.example.librarysystem.entity.Book;
import com.example.librarysystem.entity.User;
import org.springframework.stereotype.Component; // Możemy oznaczyć jako komponent Springa

import java.time.LocalDate;

@Component("standardLoanPolicy") // Nazwa beana, jeśli chcemy wstrzykiwać konkretną implementację
public class StandardLoanPolicy implements LoanPolicy {

    private static final int STANDARD_LOAN_DURATION_DAYS = 14;

    @Override
    public LocalDate calculateDueDate(LocalDate borrowDate, Book book, User user) {
        // Standardowa polityka: 14 dni dla wszystkich
        return borrowDate.plusDays(STANDARD_LOAN_DURATION_DAYS);
    }
}