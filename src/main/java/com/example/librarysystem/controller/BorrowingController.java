package com.example.librarysystem.controller;

import com.example.librarysystem.entity.Borrowing;
import com.example.librarysystem.service.BorrowingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/borrowings")
@Tag(name = "Borrowings", description = "Endpoints for managing book borrowings")
public class BorrowingController {

    private final BorrowingService borrowingService;

    public BorrowingController(BorrowingService borrowingService) {
        this.borrowingService = borrowingService;
    }

    @Operation(summary = "Borrow a book", description = "Creates a new borrowing record for a user and a book.")
    @PostMapping("/borrow")
    // @PreAuthorize("hasRole('USER') or hasRole('ADMIN')") // Użytkownik lub admin może zainicjować wypożyczenie
    public ResponseEntity<?> borrowBook(
            @Parameter(description = "ID of the user borrowing the book", required = true) @RequestParam Long userId,
            @Parameter(description = "ID of the book to be borrowed", required = true) @RequestParam Long bookId) {
        try {
            Borrowing borrowing = borrowingService.borrowBook(userId, bookId);
            return new ResponseEntity<>(borrowing, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            // Np. User not found, Book not found, Book not available
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Return a book", description = "Marks a borrowed book as returned.")
    @PutMapping("/{borrowingId}/return")
    // @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> returnBook(
            @Parameter(description = "ID of the borrowing record to be marked as returned", required = true)
            @PathVariable Long borrowingId) {
        try {
            Borrowing borrowing = borrowingService.returnBook(borrowingId);
            return ResponseEntity.ok(borrowing);
        } catch (RuntimeException e) {
            // Np. Borrowing record not found, Book already returned
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Get all borrowing records", description = "Retrieve a list of all borrowing records. (Admin only - to be secured)")
    @GetMapping
    // @PreAuthorize("hasRole('ADMIN')")
    public List<Borrowing> getAllBorrowings() {
        return borrowingService.getAllBorrowings();
    }

    @Operation(summary = "Get borrowing records for a specific user", description = "Retrieve all borrowing records for a given user. (Admin or self - to be secured)")
    @GetMapping("/user/{userId}")
    // @PreAuthorize("hasRole('ADMIN') or @userService.isSelf(authentication, #userId)")
    public ResponseEntity<?> getBorrowingsForUser(
            @Parameter(description = "ID of the user whose borrowings are to be retrieved", required = true)
            @PathVariable Long userId) {
        try {
            // Pamiętaj, że metoda getBorrowingsForUser w BorrowingService wymagała dopracowania
            // (np. przez dodanie metody do repozytorium).
            // Na razie może rzucać UnsupportedOperationException.
            List<Borrowing> borrowings = borrowingService.getBorrowingsForUser(userId);
            return ResponseEntity.ok(borrowings);
        } catch (UnsupportedOperationException e) {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(e.getMessage());
        }
        catch (RuntimeException e) { // Np. User not found
            return ResponseEntity.notFound().build();
        }
    }
}