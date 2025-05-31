package com.example.librarysystem.repository;

import com.example.librarysystem.entity.Borrowing;
import com.example.librarysystem.entity.Book;
import com.example.librarysystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BorrowingRepository extends JpaRepository<Borrowing, Long> {
    List<Borrowing> findByUser(User user);
    // Przykładowe niestandardowe metody, które mogą się przydać:
    // List<Borrowing> findByUserAndReturnDateIsNull(User user); // Aktywne wypożyczenia użytkownika
    // List<Borrowing> findByBookAndReturnDateIsNull(Book book); // Aktywne wypożyczenia danej książki
    // List<Borrowing> findByDueDateBeforeAndReturnDateIsNull(LocalDate date); // Przeterminowane wypożyczenia
}