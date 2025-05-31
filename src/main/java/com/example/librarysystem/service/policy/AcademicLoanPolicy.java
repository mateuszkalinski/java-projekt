package com.example.librarysystem.service.policy;

import com.example.librarysystem.entity.Book;
import com.example.librarysystem.entity.User;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component("academicLoanPolicy")
public class AcademicLoanPolicy implements LoanPolicy {

    private static final int ACADEMIC_LOAN_DURATION_DAYS = 30;
    private static final int SHORT_LOAN_FOR_BESTSELLERS_DAYS = 7;

    @Override
    public LocalDate calculateDueDate(LocalDate borrowDate, Book book, User user) {
        // Przykład bardziej złożonej logiki:
        // Jeśli książka jest oznaczona jako "bestseller" w gatunku (fikcyjne założenie),
        // to czas wypożyczenia jest krótszy, nawet dla polityki akademickiej.
        if (book.getGenre() != null && book.getGenre().equalsIgnoreCase("BESTSELLER")) {
            return borrowDate.plusDays(SHORT_LOAN_FOR_BESTSELLERS_DAYS);
        }
        // Dla innych książek w ramach polityki akademickiej - dłuższy czas
        return borrowDate.plusDays(ACADEMIC_LOAN_DURATION_DAYS);
    }
}