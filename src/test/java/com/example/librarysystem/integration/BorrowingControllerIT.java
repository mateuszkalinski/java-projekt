package com.example.librarysystem.integration;

import com.example.librarysystem.entity.Book;
import com.example.librarysystem.entity.Borrowing;
import com.example.librarysystem.entity.User;
import com.example.librarysystem.repository.BookRepository;
import com.example.librarysystem.repository.BorrowingRepository;
import com.example.librarysystem.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
public class BorrowingControllerIT {

    @Container
    public static PostgreSQLContainer<?> postgreSQLContainer =
            new PostgreSQLContainer<>("postgres:15-alpine")
                    .withDatabaseName("testdb_library_borrowings")
                    .withUsername("testuser")
                    .withPassword("testpass");

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BorrowingRepository borrowingRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private User adminUser;
    private Book testBook1;
    private Book testBook2;

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

        borrowingRepository.deleteAll();
        userRepository.deleteAll();
        bookRepository.deleteAll();

        // Tworzymy użytkownika „borrowUser” (rola USER)
        testUser = new User();
        testUser.setUsername("borrowUser");
        testUser.setPassword(passwordEncoder.encode("password"));
        testUser.setRole("ROLE_USER");
        userRepository.save(testUser);

        // Tworzymy użytkownika „borrowAdmin” (rola ADMIN)
        adminUser = new User();
        adminUser.setUsername("borrowAdmin");
        adminUser.setPassword(passwordEncoder.encode("password"));
        adminUser.setRole("ROLE_ADMIN");
        userRepository.save(adminUser);

        // Tworzymy dwie książki do testów
        testBook1 = new Book();
        testBook1.setTitle("Książka do Wypożyczenia 1");
        testBook1.setAuthor("Autor 1");
        testBook1.setIsbn("111-borrow");
        bookRepository.save(testBook1);

        testBook2 = new Book();
        testBook2.setTitle("Książka do Wypożyczenia 2");
        testBook2.setAuthor("Autor 2");
        testBook2.setIsbn("222-borrow");
        bookRepository.save(testBook2);
    }

    @Test
    @WithMockUser(username = "borrowUser", roles = {"USER"})
    void shouldBorrowBook_whenUserAuthenticated() throws Exception {
        mockMvc.perform(post("/api/borrowings/borrow")
                        .param("userId", testUser.getId().toString())
                        .param("bookId", testBook1.getId().toString()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(notNullValue())))
                .andExpect(jsonPath("$.user.id", is(testUser.getId().intValue())))
                .andExpect(jsonPath("$.book.id", is(testBook1.getId().intValue())))
                .andExpect(jsonPath("$.borrowDate", is(LocalDate.now().toString())))
                .andExpect(jsonPath("$.returnDate").doesNotExist());
    }

    @Test
    @WithMockUser(username = "borrowUser", roles = {"USER"})
    void shouldFailToBorrowBook_whenBookNotFound() throws Exception {
        mockMvc.perform(post("/api/borrowings/borrow")
                        .param("userId", testUser.getId().toString())
                        .param("bookId", "9999"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailToBorrowBook_whenAnonymous() throws Exception {
        mockMvc.perform(post("/api/borrowings/borrow")
                        .param("userId", "1")
                        .param("bookId", "1"))
                .andExpect(status().isFound()); // Przekierowanie do formularza logowania
    }

    @Test
    @WithMockUser(username = "borrowUser", roles = {"USER"})
    void shouldReturnBook_whenUserAuthenticated() throws Exception {
        // Przygotowujemy rekord wypożyczenia ze „starego” dnia
        Borrowing borrowing = new Borrowing(
                testUser,
                testBook1,
                LocalDate.now().minusDays(5),
                LocalDate.now().plusDays(9)
        );
        Borrowing savedBorrowing = borrowingRepository.save(borrowing);

        mockMvc.perform(put("/api/borrowings/" + savedBorrowing.getId() + "/return"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(savedBorrowing.getId().intValue())))
                .andExpect(jsonPath("$.returnDate", is(LocalDate.now().toString())));
    }

    @Test
    @WithMockUser(username = "borrowUser", roles = {"USER"})
    void shouldFailToReturnBook_whenBorrowingNotFound() throws Exception {
        mockMvc.perform(put("/api/borrowings/9999/return"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "borrowUser", roles = {"USER"})
    void shouldFailToReturnBook_whenAlreadyReturned() throws Exception {
        // Tworzymy wypożyczenie już ze zwrotem w przeszłości
        Borrowing borrowing = new Borrowing(
                testUser,
                testBook1,
                LocalDate.now().minusDays(5),
                LocalDate.now().plusDays(9)
        );
        borrowing.setReturnDate(LocalDate.now().minusDays(1));
        Borrowing savedBorrowing = borrowingRepository.save(borrowing);

        mockMvc.perform(put("/api/borrowings/" + savedBorrowing.getId() + "/return"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "borrowAdmin", roles = {"ADMIN"})
    void shouldGetAllBorrowings_whenAdmin() throws Exception {
        borrowingRepository.save(new Borrowing(
                testUser,
                testBook1,
                LocalDate.now().minusDays(2),
                LocalDate.now().plusDays(12)
        ));
        borrowingRepository.save(new Borrowing(
                adminUser,
                testBook2,
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(13)
        ));

        mockMvc.perform(get("/api/borrowings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].user.username", anyOf(is(testUser.getUsername()), is(adminUser.getUsername()))));
    }

    @Test
    @WithMockUser(username = "borrowUser", roles = {"USER"})
    void shouldFailToGetAllBorrowings_whenRegularUser() throws Exception {
        mockMvc.perform(get("/api/borrowings"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "borrowUser", roles = {"USER"})
    void shouldGetBorrowingsForSelf() throws Exception {
        // Tworzymy dwa wypożyczenia: jedno dla testUser, drugie dla innego usera
        borrowingRepository.save(new Borrowing(
                testUser,
                testBook1,
                LocalDate.now().minusDays(3),
                LocalDate.now().plusDays(11)
        ));
        User otherUser = new User();
        otherUser.setUsername("otherUserBorrow");
        otherUser.setPassword(passwordEncoder.encode("password"));
        otherUser.setRole("ROLE_USER");
        userRepository.save(otherUser);

        borrowingRepository.save(new Borrowing(
                otherUser,
                testBook2,
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(13)
        ));

        mockMvc.perform(get("/api/borrowings/user/" + testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].user.id", is(testUser.getId().intValue())))
                .andExpect(jsonPath("$[0].book.title", is(testBook1.getTitle())));
    }
}
