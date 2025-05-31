package com.example.librarysystem.service;

import com.example.librarysystem.entity.Book;
import com.example.librarysystem.entity.Borrowing;
import com.example.librarysystem.entity.User;
import com.example.librarysystem.repository.BookRepository;
import com.example.librarysystem.repository.BorrowingRepository;
import com.example.librarysystem.repository.UserRepository;
import com.example.librarysystem.service.policy.LoanPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BorrowingServiceTest {

    @Mock
    private BorrowingRepository borrowingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private LoanPolicy loanPolicy;

    @InjectMocks
    private BorrowingService borrowingService;

    private User user;
    private Book book;
    private Borrowing borrowing1;
    private LocalDate today;
    private LocalDate dueDateFromPolicy;

    @BeforeEach
    void setUp() {
        today = LocalDate.now();
        dueDateFromPolicy = today.plusDays(14);

        user = new User();
        user.setId(1L);
        user.setUsername("testUser");
        user.setRole("ROLE_USER");

        book = new Book();
        book.setId(1L);
        book.setTitle("Test Book");
        book.setAuthor("Test Author");
        book.setIsbn("1234567890");

        borrowing1 = new Borrowing();
        borrowing1.setId(1L);
        borrowing1.setUser(user);
        borrowing1.setBook(book);
        borrowing1.setBorrowDate(today);
        // dueDate wypełniamy w poszczególnych testach
    }

    @Test
    @DisplayName("borrowBook - powinien pomyślnie wypożyczyć książkę")
    void testBorrowBook_success() {
        when(loanPolicy.calculateDueDate(any(LocalDate.class), any(Book.class), any(User.class)))
                .thenReturn(dueDateFromPolicy);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(borrowingRepository.save(any(Borrowing.class))).thenAnswer(invocation -> {
            Borrowing b = invocation.getArgument(0);
            if (b.getId() == null) {
                b.setId(2L);
            }
            assertEquals(today, b.getBorrowDate());
            assertEquals(dueDateFromPolicy, b.getDueDate());
            return b;
        });

        Borrowing newBorrowing = borrowingService.borrowBook(1L, 1L);

        assertNotNull(newBorrowing);
        assertEquals(user, newBorrowing.getUser());
        assertEquals(book, newBorrowing.getBook());
        assertEquals(today, newBorrowing.getBorrowDate());
        assertEquals(dueDateFromPolicy, newBorrowing.getDueDate());
        assertNull(newBorrowing.getReturnDate());

        verify(userRepository, times(1)).findById(1L);
        verify(bookRepository, times(1)).findById(1L);
        verify(borrowingRepository, times(1)).save(any(Borrowing.class));
        verify(loanPolicy, times(1)).calculateDueDate(eq(today), eq(book), eq(user));
    }

    @Test
    @DisplayName("borrowBook - powinien rzucić wyjątek, gdy użytkownik nie istnieje")
    void testBorrowBook_userNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            borrowingService.borrowBook(1L, 1L);
        });
        assertEquals("User not found with id: 1", exception.getMessage());

        verify(bookRepository, never()).findById(anyLong());
        verify(borrowingRepository, never()).save(any(Borrowing.class));
        verify(loanPolicy, never()).calculateDueDate(any(), any(), any());
    }

    @Test
    @DisplayName("borrowBook - powinien rzucić wyjątek, gdy książka nie istnieje")
    void testBorrowBook_bookNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            borrowingService.borrowBook(1L, 1L);
        });
        assertEquals("Book not found with id: 1", exception.getMessage());

        verify(borrowingRepository, never()).save(any(Borrowing.class));
        verify(loanPolicy, never()).calculateDueDate(any(), any(), any());
    }

    @Test
    @DisplayName("returnBook - powinien pomyślnie zwrócić książkę")
    void testReturnBook_success() {
        borrowing1.setDueDate(dueDateFromPolicy);
        borrowing1.setReturnDate(null);
        when(borrowingRepository.findById(1L)).thenReturn(Optional.of(borrowing1));
        when(borrowingRepository.save(any(Borrowing.class))).thenReturn(borrowing1);

        Borrowing returnedBorrowing = borrowingService.returnBook(1L);

        assertNotNull(returnedBorrowing);
        assertEquals(today, returnedBorrowing.getReturnDate());
        verify(borrowingRepository, times(1)).save(borrowing1);
        verify(loanPolicy, never()).calculateDueDate(any(), any(), any());
    }

    @Test
    @DisplayName("returnBook - powinien rzucić wyjątek, gdy wypożyczenie nie istnieje")
    void testReturnBook_borrowingNotFound() {
        when(borrowingRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            borrowingService.returnBook(1L);
        });
        assertEquals("Borrowing record not found with id: 1", exception.getMessage());
        verify(loanPolicy, never()).calculateDueDate(any(), any(), any());
    }

    @Test
    @DisplayName("returnBook - powinien rzucić wyjątek, gdy książka już została zwrócona")
    void testReturnBook_alreadyReturned() {
        borrowing1.setDueDate(dueDateFromPolicy);
        borrowing1.setReturnDate(today.minusDays(1));
        when(borrowingRepository.findById(1L)).thenReturn(Optional.of(borrowing1));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            borrowingService.returnBook(1L);
        });
        assertTrue(exception.getMessage().contains("Book already returned on"));
        verify(loanPolicy, never()).calculateDueDate(any(), any(), any());
    }

    @Test
    @DisplayName("getAllBorrowings - powinien zwrócić listę wszystkich wypożyczeń")
    void testGetAllBorrowings() {
        Borrowing borrowing2 = new Borrowing(user, book, today.minusDays(5), today.plusDays(9));
        borrowing2.setId(2L);
        borrowing1.setDueDate(dueDateFromPolicy);
        when(borrowingRepository.findAll()).thenReturn(Arrays.asList(borrowing1, borrowing2));

        List<Borrowing> borrowings = borrowingService.getAllBorrowings();

        assertNotNull(borrowings);
        assertEquals(2, borrowings.size());
        verify(borrowingRepository, times(1)).findAll();
        verify(loanPolicy, never()).calculateDueDate(any(), any(), any());
    }

    @Test
    @DisplayName("getBorrowingsForUser - powinien zwrócić wypożyczenia dla danego użytkownika")
    void testGetBorrowingsForUser_success() {
        borrowing1.setDueDate(dueDateFromPolicy);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(borrowingRepository.findByUser(user)).thenReturn(Arrays.asList(borrowing1));

        List<Borrowing> userBorrowings = borrowingService.getBorrowingsForUser(1L);

        assertNotNull(userBorrowings);
        assertEquals(1, userBorrowings.size());
        assertEquals(borrowing1, userBorrowings.get(0));
        verify(userRepository, times(1)).findById(1L);
        verify(borrowingRepository, times(1)).findByUser(user);
        verify(loanPolicy, never()).calculateDueDate(any(), any(), any());
    }

    @Test
    @DisplayName("getBorrowingsForUser - powinien rzucić wyjątek, gdy użytkownik nie istnieje")
    void testGetBorrowingsForUser_userNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            borrowingService.getBorrowingsForUser(1L);
        });
        assertEquals("User not found with id: 1", exception.getMessage());
        verify(borrowingRepository, never()).findByUser(any(User.class));
        verify(loanPolicy, never()).calculateDueDate(any(), any(), any());
    }
}
