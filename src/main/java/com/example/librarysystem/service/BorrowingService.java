package com.example.librarysystem.service;

import com.example.librarysystem.entity.Book;
import com.example.librarysystem.entity.Borrowing;
import com.example.librarysystem.entity.User;
import com.example.librarysystem.repository.BookRepository;
import com.example.librarysystem.repository.BorrowingRepository;
import com.example.librarysystem.repository.UserRepository;
import com.example.librarysystem.service.policy.LoanPolicy;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class BorrowingService {

    private final BorrowingRepository borrowingRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final LoanPolicy loanPolicy;

    // Wstrzykujemy politykę pożyczek (np. standardLoanPolicy)
    public BorrowingService(BorrowingRepository borrowingRepository,
                            UserRepository userRepository,
                            BookRepository bookRepository,
                            @Qualifier("standardLoanPolicy") LoanPolicy loanPolicy) {
        this.borrowingRepository = borrowingRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.loanPolicy = loanPolicy;
    }

    @Transactional
    public Borrowing borrowBook(Long userId, Long bookId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + bookId));

        // TODO: Dodać logiczną weryfikację dostępności (np. book.getCopiesAvailable())
        LocalDate borrowDate = LocalDate.now();
        LocalDate dueDate = loanPolicy.calculateDueDate(borrowDate, book, user);

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
        // TODO: Tu dodać np. aktualizację dostępnych kopii książki
        return borrowingRepository.save(borrowing);
    }

    @Transactional(readOnly = true)
    public List<Borrowing> getAllBorrowings() {
        return borrowingRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Borrowing> getBorrowingsForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return borrowingRepository.findByUser(user);
    }
}
