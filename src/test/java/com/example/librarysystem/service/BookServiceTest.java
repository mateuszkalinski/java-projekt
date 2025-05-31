package com.example.librarysystem.service;

import com.example.librarysystem.entity.Book;
import com.example.librarysystem.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookService bookService;

    private Book book1;
    private Book book2;

    @BeforeEach
    void setUp() {
        book1 = new Book();
        book1.setId(1L);
        book1.setTitle("Władca Pierścieni");
        book1.setAuthor("J.R.R. Tolkien");
        book1.setIsbn("978-0618260274");

        book2 = new Book();
        book2.setId(2L);
        book2.setTitle("Hobbit");
        book2.setAuthor("J.R.R. Tolkien");
        book2.setIsbn("978-0547928227");
    }

    @Test
    @DisplayName("Powinien zwrócić wszystkie książki")
    void testGetAllBooks() {
        when(bookRepository.findAll()).thenReturn(Arrays.asList(book1, book2));

        List<Book> books = bookService.getAllBooks();

        assertNotNull(books);
        assertEquals(2, books.size());
        verify(bookRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Powinien zwrócić książkę po ID, gdy książka istnieje")
    void testGetBookById_whenBookExists() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book1));

        Optional<Book> foundBookOptional = bookService.getBookById(1L);

        assertTrue(foundBookOptional.isPresent());
        assertEquals(book1.getTitle(), foundBookOptional.get().getTitle());
        verify(bookRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Powinien zwrócić pusty Optional, gdy książka o danym ID nie istnieje")
    void testGetBookById_whenBookDoesNotExist() {
        when(bookRepository.findById(3L)).thenReturn(Optional.empty());

        Optional<Book> foundBookOptional = bookService.getBookById(3L);

        assertFalse(foundBookOptional.isPresent());
        verify(bookRepository, times(1)).findById(3L);
    }

    @Test
    @DisplayName("Powinien dodać nową książkę")
    void testAddBook() {
        Book newBook = new Book();
        newBook.setTitle("Nowa Książka");
        newBook.setAuthor("Nowy Autor");
        newBook.setIsbn("123-4567890123");

        // Symulacja zapisu i zwrócenia obiektu z nadanym ID (lub po prostu zwrócenie argumentu)
        when(bookRepository.save(any(Book.class))).thenReturn(newBook); // Można też użyć thenAnswer jak wcześniej

        Book createdBook = bookService.addBook(newBook);

        assertNotNull(createdBook);
        assertEquals("Nowa Książka", createdBook.getTitle());
        verify(bookRepository, times(1)).save(newBook);
    }

    @Test
    @DisplayName("Powinien zaktualizować istniejącą książkę")
    void testUpdateBook_whenBookExists() {
        Book bookDetailsToUpdate = new Book();
        bookDetailsToUpdate.setTitle("Zaktualizowany Władca Pierścieni");
        bookDetailsToUpdate.setAuthor(book1.getAuthor()); // Załóżmy, że autor się nie zmienia
        bookDetailsToUpdate.setIsbn(book1.getIsbn());   // ISBN też

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book1));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));


        Book updatedBook = bookService.updateBook(1L, bookDetailsToUpdate);

        assertNotNull(updatedBook);
        assertEquals("Zaktualizowany Władca Pierścieni", updatedBook.getTitle());
        assertEquals(1L, updatedBook.getId());
        verify(bookRepository, times(1)).findById(1L);
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    @DisplayName("Powinien rzucić wyjątek podczas aktualizacji, gdy książka nie istnieje")
    void testUpdateBook_whenBookDoesNotExist() {
        Book bookDetailsToUpdate = new Book();
        bookDetailsToUpdate.setTitle("Nieistniejąca Książka");
        when(bookRepository.findById(3L)).thenReturn(Optional.empty());

        // Sprawdzamy, czy rzucany jest oczekiwany wyjątek
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bookService.updateBook(3L, bookDetailsToUpdate);
        });
        assertEquals("Book not found with id: 3", exception.getMessage());

        verify(bookRepository, times(1)).findById(3L);
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    @DisplayName("Powinien usunąć książkę po ID, gdy książka istnieje")
    void testDeleteBook_whenBookExists() {
        when(bookRepository.existsById(1L)).thenReturn(true);
        doNothing().when(bookRepository).deleteById(1L);

        assertDoesNotThrow(() -> bookService.deleteBook(1L));

        verify(bookRepository, times(1)).existsById(1L);
        verify(bookRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Powinien rzucić wyjątek podczas usuwania, gdy książka nie istnieje")
    void testDeleteBook_whenBookDoesNotExist() {
        when(bookRepository.existsById(3L)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bookService.deleteBook(3L);
        });
        assertEquals("Book not found with id: 3", exception.getMessage());

        verify(bookRepository, times(1)).existsById(3L);
        verify(bookRepository, never()).deleteById(anyLong());
    }
}