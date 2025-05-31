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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Jednostkowe testy logiki BookService.
 */
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
        book1.setGenre("Fantasy");
        book1.setPublisher("Allen & Unwin");
        book1.setPublicationYear(1954);

        book2 = new Book();
        book2.setId(2L);
        book2.setTitle("Hobbit");
        book2.setAuthor("J.R.R. Tolkien");
        book2.setIsbn("978-0547928227");
    }

    @Test
    @DisplayName("getAllBooks - powinien zwrócić wszystkie książki")
    void testGetAllBooks_shouldReturnAllBooks() {
        when(bookRepository.findAll()).thenReturn(Arrays.asList(book1, book2));

        List<Book> books = bookService.getAllBooks();

        assertNotNull(books);
        assertEquals(2, books.size());
        assertTrue(books.contains(book1));
        assertTrue(books.contains(book2));
        verify(bookRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getAllBooks - powinien zwrócić pustą listę, gdy nie ma książek")
    void testGetAllBooks_shouldReturnEmptyListWhenNoBooks() {
        when(bookRepository.findAll()).thenReturn(Collections.emptyList());

        List<Book> books = bookService.getAllBooks();

        assertNotNull(books);
        assertTrue(books.isEmpty());
        verify(bookRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getBookById - powinien zwrócić książkę, gdy ID istnieje")
    void testGetBookById_whenBookExists() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book1));

        Optional<Book> foundBookOptional = bookService.getBookById(1L);

        assertTrue(foundBookOptional.isPresent());
        assertEquals(book1.getTitle(), foundBookOptional.get().getTitle());
        verify(bookRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("getBookById - powinien zwrócić pusty Optional, gdy ID nie istnieje")
    void testGetBookById_whenBookDoesNotExist() {
        when(bookRepository.findById(3L)).thenReturn(Optional.empty());

        Optional<Book> foundBookOptional = bookService.getBookById(3L);

        assertFalse(foundBookOptional.isPresent());
        verify(bookRepository, times(1)).findById(3L);
    }

    @Test
    @DisplayName("addBook - powinien dodać nową książkę, gdy ISBN jest unikalny")
    void testAddBook_shouldAddNewBookWhenIsbnIsUnique() {
        Book newBook = new Book();
        newBook.setTitle("Nowa Książka");
        newBook.setAuthor("Nowy Autor");
        newBook.setIsbn("123-4567890123");

        when(bookRepository.findByIsbn(newBook.getIsbn())).thenReturn(Optional.empty());
        when(bookRepository.save(any(Book.class))).thenReturn(newBook);

        Book createdBook = bookService.addBook(newBook);

        assertNotNull(createdBook);
        assertEquals("Nowa Książka", createdBook.getTitle());
        verify(bookRepository, times(1)).findByIsbn(newBook.getIsbn());
        verify(bookRepository, times(1)).save(newBook);
    }

    @Test
    @DisplayName("addBook - powinien rzucić wyjątek, gdy ISBN już istnieje")
    void testAddBook_shouldThrowExceptionWhenIsbnExists() {
        Book existingBook = new Book();
        existingBook.setIsbn("111-222333444");
        existingBook.setTitle("Istniejąca książka");

        Book newBookTryingToAdd = new Book();
        newBookTryingToAdd.setIsbn("111-222333444");
        newBookTryingToAdd.setTitle("Nowa książka z tym samym ISBN");

        when(bookRepository.findByIsbn(newBookTryingToAdd.getIsbn()))
                .thenReturn(Optional.of(existingBook));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            bookService.addBook(newBookTryingToAdd);
        });

        assertEquals("Book with ISBN " + newBookTryingToAdd.getIsbn() + " already exists.", exception.getMessage());
        verify(bookRepository, times(1)).findByIsbn(newBookTryingToAdd.getIsbn());
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    @DisplayName("updateBook - powinien zaktualizować wszystkie pola istniejącej książki")
    void testUpdateBook_shouldUpdateAllFieldsWhenBookExists() {
        Book bookDetailsToUpdate = new Book();
        bookDetailsToUpdate.setTitle("Zaktualizowany Władca Pierścieni");
        bookDetailsToUpdate.setAuthor("J. R. R. Tolkien Nowe Wydanie");
        bookDetailsToUpdate.setIsbn("978-0000000001");
        bookDetailsToUpdate.setPublisher("Nowy Wydawca");
        bookDetailsToUpdate.setPublicationYear(2025);
        bookDetailsToUpdate.setGenre("High Fantasy");

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book1));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Book updatedBook = bookService.updateBook(1L, bookDetailsToUpdate);

        assertNotNull(updatedBook);
        assertEquals(1L, updatedBook.getId());
        assertEquals("Zaktualizowany Władca Pierścieni", updatedBook.getTitle());
        assertEquals("J. R. R. Tolkien Nowe Wydanie", updatedBook.getAuthor());
        assertEquals("978-0000000001", updatedBook.getIsbn());
        assertEquals("Nowy Wydawca", updatedBook.getPublisher());
        assertEquals(2025, updatedBook.getPublicationYear());
        assertEquals("High Fantasy", updatedBook.getGenre());

        verify(bookRepository, times(1)).findById(1L);
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    @DisplayName("updateBook - powinien rzucić wyjątek, gdy aktualizowana książka nie istnieje")
    void testUpdateBook_shouldThrowExceptionWhenBookDoesNotExist() {
        Book bookDetailsToUpdate = new Book();
        bookDetailsToUpdate.setTitle("Nieistniejąca Książka Aktualizacja");
        when(bookRepository.findById(3L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bookService.updateBook(3L, bookDetailsToUpdate);
        });
        assertEquals("Book not found with id: 3", exception.getMessage());
        verify(bookRepository, times(1)).findById(3L);
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    @DisplayName("deleteBook - powinien usunąć książkę, gdy ID istnieje")
    void testDeleteBook_shouldDeleteBookWhenBookExists() {
        when(bookRepository.existsById(1L)).thenReturn(true);
        doNothing().when(bookRepository).deleteById(1L);

        assertDoesNotThrow(() -> bookService.deleteBook(1L));
        verify(bookRepository, times(1)).existsById(1L);
        verify(bookRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("deleteBook - powinien rzucić wyjątek, gdy usuwana książka nie istnieje")
    void testDeleteBook_shouldThrowExceptionWhenBookDoesNotExist() {
        when(bookRepository.existsById(3L)).thenReturn(false);
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bookService.deleteBook(3L);
        });
        assertEquals("Book not found with id: 3", exception.getMessage());
        verify(bookRepository, times(1)).existsById(3L);
        verify(bookRepository, never()).deleteById(anyLong());
    }
}
