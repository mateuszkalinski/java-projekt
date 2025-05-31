package com.example.librarysystem.integration;

import com.example.librarysystem.entity.Book;
import com.example.librarysystem.repository.BookRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integracyjne testy kontrolera BookController.
 *
 * – Tworzenie nowej książki → ADMIN (201).
 * – Próba stworzenia → USER (403).
 * – GET /api/books/{id} → USER (200), ANONYMOUS → 302.
 * – GET /api/books → USER (200), ANONYMOUS → 302.
 * – PUT/DELETE → ADMIN ok (200/204), USER → 403, ANONYMOUS → 302.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
public class BookControllerIT {

    @Container
    public static PostgreSQLContainer<?> postgreSQLContainer =
            new PostgreSQLContainer<>("postgres:15-alpine")
                    .withDatabaseName("testdb_library_book")
                    .withUsername("testuser")
                    .withPassword("testpass");

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookRepository bookRepository;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.flyway.enabled", () -> false);
    }

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(this.webApplicationContext)
                .apply(springSecurity())
                .build();

        bookRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldCreateNewBook_whenAdmin() throws Exception {
        Book newBook = new Book();
        newBook.setTitle("Nowa Książka Integracyjna");
        newBook.setAuthor("Autor Testowy");
        newBook.setIsbn("999-1234567890");

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newBook)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(notNullValue())))
                .andExpect(jsonPath("$.title", is("Nowa Książka Integracyjna")))
                .andExpect(jsonPath("$.author", is("Autor Testowy")));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldFailToCreateNewBook_whenUser() throws Exception {
        Book newBook = new Book();
        newBook.setTitle("Książka Użytkownika");
        newBook.setAuthor("Autor Użytkownik");
        newBook.setIsbn("888-1234567890");

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newBook)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldGetBookById() throws Exception {
        Book bookToCreate = new Book();
        bookToCreate.setTitle("Książka Do Odczytu");
        bookToCreate.setAuthor("Autor Do Odczytu");
        bookToCreate.setIsbn("777-1234567890");
        Book savedBook = bookRepository.save(bookToCreate);

        mockMvc.perform(get("/api/books/" + savedBook.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(savedBook.getId().intValue())))
                .andExpect(jsonPath("$.title", is("Książka Do Odczytu")));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldGetAllBooks() throws Exception {
        Book book1 = new Book();
        book1.setTitle("Książka Alpha");
        book1.setAuthor("Autor Alpha");
        book1.setIsbn("111-123");
        bookRepository.save(book1);

        Book book2 = new Book();
        book2.setTitle("Książka Beta");
        book2.setAuthor("Autor Beta");
        book2.setIsbn("222-123");
        bookRepository.save(book2);

        mockMvc.perform(get("/api/books")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].title", containsInAnyOrder("Książka Alpha", "Książka Beta")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldUpdateExistingBook_whenAdmin() throws Exception {
        Book bookToCreate = new Book();
        bookToCreate.setTitle("Książka Do Aktualizacji");
        bookToCreate.setAuthor("Autor Do Aktualizacji");
        bookToCreate.setIsbn("333-123");
        Book savedBook = bookRepository.save(bookToCreate);

        Book updatedBookDetails = new Book();
        updatedBookDetails.setTitle("Zaktualizowana Nazwa Książki");
        updatedBookDetails.setAuthor(savedBook.getAuthor());
        updatedBookDetails.setIsbn(savedBook.getIsbn());

        mockMvc.perform(put("/api/books/" + savedBook.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedBookDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(savedBook.getId().intValue())))
                .andExpect(jsonPath("$.title", is("Zaktualizowana Nazwa Książki")));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldFailToUpdateBook_whenUser() throws Exception {
        Book bookToCreate = new Book();
        bookToCreate.setTitle("Książka Do Aktualizacji przez Usera");
        bookToCreate.setAuthor("Autor");
        bookToCreate.setIsbn("333-456");
        Book savedBook = bookRepository.save(bookToCreate);

        Book updatedBookDetails = new Book();
        updatedBookDetails.setTitle("Nieudana Aktualizacja");

        mockMvc.perform(put("/api/books/" + savedBook.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedBookDetails)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldDeleteExistingBook_whenAdmin() throws Exception {
        Book bookToCreate = new Book();
        bookToCreate.setTitle("Książka Do Usunięcia");
        bookToCreate.setAuthor("Autor Do Usunięcia");
        bookToCreate.setIsbn("444-123");
        Book savedBook = bookRepository.save(bookToCreate);

        mockMvc.perform(delete("/api/books/" + savedBook.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/books/" + savedBook.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldFailToDeleteBook_whenUser() throws Exception {
        Book bookToCreate = new Book();
        bookToCreate.setTitle("Książka Do Usunięcia przez Usera");
        bookToCreate.setAuthor("Autor");
        bookToCreate.setIsbn("444-456");
        Book savedBook = bookRepository.save(bookToCreate);

        mockMvc.perform(delete("/api/books/" + savedBook.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldFailToCreateBook_whenAnonymous() throws Exception {
        Book newBook = new Book();
        newBook.setTitle("Anonimowa Książka");
        newBook.setAuthor("Anonimowy Autor");
        newBook.setIsbn("000-000");

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newBook)))
                .andExpect(status().isFound()); // 302 – przekierowanie do logowania
    }

    @Test
    void shouldFailToGetBooks_whenAnonymous() throws Exception {
        mockMvc.perform(get("/api/books")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isFound()); // 302 – przekierowanie do logowania
    }
}
