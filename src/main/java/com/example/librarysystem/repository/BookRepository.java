package com.example.librarysystem.repository;

import com.example.librarysystem.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional; // Możemy potrzebować do wyszukiwania po ISBN

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    // Możemy tutaj dodać niestandardowe metody zapytań w przyszłości, np.:
    // Optional<Book> findByIsbn(String isbn);
    // List<Book> findByAuthor(String author);
    // List<Book> findByTitleContainingIgnoreCase(String titleKeyword);
}