package com.example.librarysystem.service;

import com.example.librarysystem.entity.Book;
import com.example.librarysystem.repository.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Ważne dla operacji zapisu

import java.util.List;
import java.util.Optional;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Transactional(readOnly = true) // Dobra praktyka dla metod tylko do odczytu
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Book> getBookById(Long id) {
        return bookRepository.findById(id);
    }

    // Przykład metody do wyszukiwania po ISBN (jeśli dodasz ją do BookRepository)
    // @Transactional(readOnly = true)
    // public Optional<Book> getBookByIsbn(String isbn) {
    //     return bookRepository.findByIsbn(isbn);
    // }

    @Transactional // Operacje zapisu/modyfikacji powinny być transakcyjne
    public Book addBook(Book book) {
        // Tutaj można dodać walidację, np. czy książka o danym ISBN już istnieje
        // Optional<Book> existingBook = bookRepository.findByIsbn(book.getIsbn());
        // if (existingBook.isPresent()) {
        //     throw new IllegalStateException("Book with ISBN " + book.getIsbn() + " already exists.");
        // }
        return bookRepository.save(book);
    }

    @Transactional
    public Book updateBook(Long id, Book bookDetails) {
        Book bookToUpdate = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + id)); // Rozważ dedykowany wyjątek, np. ResourceNotFoundException

        bookToUpdate.setTitle(bookDetails.getTitle());
        bookToUpdate.setAuthor(bookDetails.getAuthor());
        bookToUpdate.setIsbn(bookDetails.getIsbn());
        bookToUpdate.setPublisher(bookDetails.getPublisher());
        bookToUpdate.setPublicationYear(bookDetails.getPublicationYear());
        bookToUpdate.setGenre(bookDetails.getGenre());
        // Jeśli dodałeś pola totalCopies/availableCopies, też je tutaj zaktualizuj
        // bookToUpdate.setTotalCopies(bookDetails.getTotalCopies());
        // bookToUpdate.setAvailableCopies(bookDetails.getAvailableCopies());

        return bookRepository.save(bookToUpdate);
    }

    @Transactional
    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new RuntimeException("Book not found with id: " + id); // Lub dedykowany wyjątek
        }
        bookRepository.deleteById(id);
    }

}