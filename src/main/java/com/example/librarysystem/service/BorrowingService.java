package com.example.librarysystem.service;

import com.example.librarysystem.entity.Book;
import com.example.librarysystem.entity.Borrowing;
import com.example.librarysystem.entity.User;
import com.example.librarysystem.repository.BookRepository;
import com.example.librarysystem.repository.BorrowingRepository;
import com.example.librarysystem.repository.UserRepository;
import com.example.librarysystem.service.policy.LoanPolicy; // <--- IMPORT
import org.springframework.beans.factory.annotation.Qualifier; // <--- IMPORT
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class BorrowingService {

    private final BorrowingRepository borrowingRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final LoanPolicy loanPolicy; // <--- Pole dla polityki

    // Wstrzykujemy konkretną politykę przez @Qualifier lub Spring wybierze, jeśli jest tylko jedna
    // lub oznaczona jako @Primary.
    public BorrowingService(BorrowingRepository borrowingRepository,
                            UserRepository userRepository,
                            BookRepository bookRepository,
                            @Qualifier("standardLoanPolicy") LoanPolicy loanPolicy) { // <--- Wstrzyknięcie
        this.borrowingRepository = borrowingRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.loanPolicy = loanPolicy; // <--- Przypisanie
    }

    @Transactional
    public Borrowing borrowBook(Long userId, Long bookId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + bookId));

        // TODO: Logika sprawdzająca dostępność książki (np. availableCopies)

        LocalDate borrowDate = LocalDate.now();
        // Użycie wstrzykniętej polityki do obliczenia terminu zwrotu
        LocalDate dueDate = this.loanPolicy.calculateDueDate(borrowDate, book, user); // <--- Użycie

        Borrowing borrowing = new Borrowing(user, book, borrowDate, dueDate);
        return borrowingRepository.save(borrowing);
    }

    @Transactional
    public Borrowing returnBook(Long borrowingId) {
        Borrowing borrowing = borrowingRepository.findById(borrowingId)
                .orElseThrow(() -> new RuntimeException("Borrowing record not found with id: " + borrowingId));

        if (borrowing.getReturnDate() != null) {
            throw new IllegalStateException("Book already returned on " + borrowing.getReturnDate());
        }
        borrowing.setReturnDate(LocalDate.now());
        // TODO: Logika aktualizacji availableCopies w Book
        return borrowingRepository.save(borrowing);
    }

    @Transactional(readOnly = true)
    public List<Borrowing> getAllBorrowings() {
        return borrowingRepository.findAll();
    }

    // ... reszta metod ...
    // getBorrowingsForUser - pamiętaj, aby zaimplementować dedykowaną metodę w repozytorium
    // lub ulepszyć logikę pobierania.
}