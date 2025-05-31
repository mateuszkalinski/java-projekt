package com.example.librarysystem.controller;

import com.example.librarysystem.entity.Book;
import com.example.librarysystem.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
// Optional jest potrzebny, jeśli getBookById zwraca Optional<Book> i chcemy go mapować
// import java.util.Optional;

@RestController
@RequestMapping("/api/books") // Zmieniamy ścieżkę bazową na /api/books
@Tag(name = "Books", description = "Endpoints for managing books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @Operation(summary = "Get all books", description = "Retrieve a list of all books in the system")
    @GetMapping
    public List<Book> getAllBooks() {
        return bookService.getAllBooks();
    }

    @Operation(summary = "Get book by ID", description = "Retrieve a specific book by its ID")
    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(
            @Parameter(description = "ID of the book to be retrieved", required = true, example = "1")
            @PathVariable Long id) {
        return bookService.getBookById(id)
                .map(ResponseEntity::ok) // Jeśli serwis zwraca Optional<Book>
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Add a new book", description = "Create a new book with provided details. (Admin only - to be secured)")
    @PostMapping
    // @PreAuthorize("hasRole('ADMIN')") // Dodamy później ze Spring Security
    public ResponseEntity<Book> addBook(
            @Parameter(description = "Book object to create. Title, author, ISBN are typically required.", required = true)
            @RequestBody Book book) {
        Book createdBook = bookService.addBook(book);
        return new ResponseEntity<>(createdBook, HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing book", description = "Update an existing book by its ID. (Admin only - to be secured)")
    @PutMapping("/{id}")
    // @PreAuthorize("hasRole('ADMIN')") // Dodamy później
    public ResponseEntity<Book> updateBook(
            @Parameter(description = "ID of the book to update", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Book object with updated information.", required = true)
            @RequestBody Book bookDetails) {
        try {
            Book updatedBook = bookService.updateBook(id, bookDetails);
            return ResponseEntity.ok(updatedBook);
        } catch (RuntimeException e) { // Na razie łapiemy generyczny RuntimeException z serwisu
            // W przyszłości można obsłużyć specyficzne wyjątki np. ResourceNotFoundException
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Delete a book", description = "Delete a book by its ID. (Admin only - to be secured)")
    @DeleteMapping("/{id}")
    // @PreAuthorize("hasRole('ADMIN')") // Dodamy później
    public ResponseEntity<Void> deleteBook(
            @Parameter(description = "ID of the book to delete", required = true, example = "1")
            @PathVariable Long id) {
        try {
            bookService.deleteBook(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}