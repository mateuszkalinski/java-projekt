package com.example.librarysystem.repository;

import com.example.librarysystem.entity.Borrowing;
// import com.example.librarysystem.entity.Book; // Jeśli będziesz chciał np. listę wypożyczeń danej książki
// import com.example.librarysystem.entity.User; // Jeśli będziesz chciał np. listę wypożyczeń danego użytkownika
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// import java.util.List; // Do potencjalnych metod niestandardowych

@Repository
public interface BorrowingRepository extends JpaRepository<Borrowing, Long> {

    // Przykładowe niestandardowe metody, które mogą się przydać:
    // List<Borrowing> findByUserAndReturnDateIsNull(User user); // Aktywne wypożyczenia użytkownika
    // List<Borrowing> findByBookAndReturnDateIsNull(Book book); // Aktywne wypożyczenia danej książki
    // List<Borrowing> findByDueDateBeforeAndReturnDateIsNull(LocalDate date); // Przeterminowane wypożyczenia
}