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

    private Book project1;
    private Book project2;

    @BeforeEach
    void setUp() {
        project1 = new Book();
        project1.setId(1L);
        project1.setName("TestProject1");

        project2 = new Book();
        project2.setId(2L);
        project2.setName("TestProject2");
    }

    @Test
    @DisplayName("Powinien zwrócić wszystkie projekty")
    void testGetAllProjects() {
        when(bookRepository.findAll()).thenReturn(Arrays.asList(project1, project2));

        List<Book> projects = bookService.getAllProjects();

        assertNotNull(projects);
        assertEquals(2, projects.size());
        verify(bookRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Powinien zwrócić projekt po ID, gdy projekt istnieje")
    void testGetProjectById_whenProjectExists() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(project1));

        Optional<Book> foundProjectOptional = bookService.getProjectById(1L);

        assertTrue(foundProjectOptional.isPresent());
        assertEquals(project1.getName(), foundProjectOptional.get().getName());
        verify(bookRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Powinien zwrócić pusty Optional, gdy projekt o danym ID nie istnieje")
    void testGetProjectById_whenProjectDoesNotExist() {
        when(bookRepository.findById(3L)).thenReturn(Optional.empty());

        Optional<Book> foundProjectOptional = bookService.getProjectById(3L);

        assertFalse(foundProjectOptional.isPresent());
        verify(bookRepository, times(1)).findById(3L);
    }

    @Test
    @DisplayName("Powinien utworzyć nowy projekt")
    void testCreateProject() {
        Book newProject = new Book();
        newProject.setName("NewProject");

        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> {
            Book p = invocation.getArgument(0);
            if (p.getId() == null) {
                p.setId(3L);
            }
            return p;
        });

        Book createdProject = bookService.createProject(newProject);

        assertNotNull(createdProject);
        assertEquals("NewProject", createdProject.getName());
        assertNotNull(createdProject.getId());
        verify(bookRepository, times(1)).save(newProject);
    }

    @Test
    @DisplayName("Powinien zaktualizować istniejący projekt")
    void testUpdateProject_whenProjectExists() {
        Book projectDetailsToUpdate = new Book();
        projectDetailsToUpdate.setName("UpdatedProject");

        when(bookRepository.existsById(1L)).thenReturn(true);
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> {
            Book p = invocation.getArgument(0);
            if (p.getId() == null) p.setId(1L);
            return p;
        });

        Book updatedProject = bookService.updateProject(1L, projectDetailsToUpdate);

        assertNotNull(updatedProject);
        assertEquals("UpdatedProject", updatedProject.getName());
        assertEquals(1L, updatedProject.getId());
        verify(bookRepository, times(1)).existsById(1L);
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    @DisplayName("Powinien zwrócić null podczas aktualizacji, gdy projekt nie istnieje")
    void testUpdateProject_whenProjectDoesNotExist() {
        Book projectDetailsToUpdate = new Book();
        projectDetailsToUpdate.setName("UpdatedProject");
        when(bookRepository.existsById(3L)).thenReturn(false);

        Book result = bookService.updateProject(3L, projectDetailsToUpdate);

        assertNull(result);
        verify(bookRepository, times(1)).existsById(3L);
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    @DisplayName("Powinien usunąć projekt po ID, gdy projekt istnieje") // Zmieniłem trochę nazwę dla jasności
    void testDeleteProject_whenProjectExists() {
        // 1. Zaaranżuj (Arrange): Mockujemy zachowanie repozytorium
        when(bookRepository.existsById(1L)).thenReturn(true); // <-- DODAJ TEN MOCK
        doNothing().when(bookRepository).deleteById(1L);

        // 2. Działaj (Act): Wywołaj testowaną metodę
        // Użyj assertDoesNotThrow, aby upewnić się, że wyjątek nie jest rzucany,
        // gdy projekt istnieje
        assertDoesNotThrow(() -> bookService.deleteProject(1L));

        // 3. Asercje (Assert): Zweryfikuj, czy odpowiednie metody repozytorium zostały wywołane
        verify(bookRepository, times(1)).existsById(1L); // Dobrze jest też to zweryfikować
        verify(bookRepository, times(1)).deleteById(1L);
    }
}
