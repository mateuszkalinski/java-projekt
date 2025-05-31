package com.example.librarysystem.integration;

import com.example.librarysystem.entity.User;
import com.example.librarysystem.repository.UserRepository;
// import com.example.librarysystem.service.UserService; // Nie potrzebujemy bezpośrednio serwisu w teście IT kontrolera
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder; // Do przygotowania zahashowanych haseł
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
public class UserControllerIT {

    @Container
    public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb_library_users")
            .withUsername("testuser")
            .withPassword("testpass");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // Wstrzykujemy PasswordEncoder do hashowania haseł dla testów

    private User adminUser;
    private User regularUser;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @BeforeEach
    void setUp() {
        userRepository.deleteAll(); // Czyścimy repozytorium przed każdym testem

        // Tworzymy przykładowych użytkowników dla niektórych testów
        adminUser = new User();
        adminUser.setUsername("adminIT");
        adminUser.setPassword(passwordEncoder.encode("password")); // Hashujemy hasło
        adminUser.setRole("ROLE_ADMIN");
        userRepository.save(adminUser);

        regularUser = new User();
        regularUser.setUsername("userIT");
        regularUser.setPassword(passwordEncoder.encode("password"));
        regularUser.setRole("ROLE_USER");
        userRepository.save(regularUser);
    }

    @Test
    void shouldRegisterNewUser() throws Exception {
        User newUser = new User();
        newUser.setUsername("newUserRegister");
        newUser.setPassword("plainPassword123"); // Hasło w formie jawnej, serwis je zahashuje
        newUser.setRole("ROLE_USER"); // Rola może być też ustawiana domyślnie w serwisie

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(notNullValue())))
                .andExpect(jsonPath("$.username", is("newUserRegister")))
                .andExpect(jsonPath("$.password").doesNotExist()); // WAŻNE: Hasło nie powinno być zwracane!
        // To wymaga DTO lub odpowiedniej konfiguracji Jacksona (@JsonIgnore)
        // Jeśli encja User jest zwracana bezpośrednio, ten test może failować
        // lub trzeba go dostosować do aktualnego zachowania
    }

    @Test
    @WithMockUser(username = "adminIT", roles = {"ADMIN"})
    void shouldGetAllUsers_whenAdmin() throws Exception {
        mockMvc.perform(get("/api/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2))) // adminIT i userIT
                .andExpect(jsonPath("$[0].username").exists())
                .andExpect(jsonPath("$[0].password").doesNotExist()) // Spodziewamy się braku hasła
                .andExpect(jsonPath("$[1].username").exists())
                .andExpect(jsonPath("$[1].password").doesNotExist());
    }

    @Test
    @WithMockUser(username = "userIT", roles = {"USER"})
    void shouldFailToGetAllUsers_whenRegularUser() throws Exception {
        mockMvc.perform(get("/api/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "adminIT", roles = {"ADMIN"})
    void shouldGetUserById_whenAdmin() throws Exception {
        mockMvc.perform(get("/api/users/" + regularUser.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(regularUser.getId().intValue())))
                .andExpect(jsonPath("$.username", is(regularUser.getUsername())))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    // Test dla użytkownika próbującego pobrać własne dane (wymagałby bardziej złożonej logiki @PreAuthorize)
    // @Test
    // @WithMockUser(username = "userIT", roles = {"USER"})
    // void shouldGetUserById_whenGettingSelf() throws Exception { ... }


    @Test
    @WithMockUser(username = "adminIT", roles = {"ADMIN"})
    void shouldUpdateUser_whenAdmin() throws Exception {
        User userDetailsToUpdate = new User();
        userDetailsToUpdate.setUsername("userIT_updated");
        // Hasło nie jest aktualizowane w tym teście, aby sprawdzić aktualizację innych pól
        // Jeśli chcemy aktualizować hasło, trzeba je podać w formie jawnej.
        userDetailsToUpdate.setRole("ROLE_USER"); // Rola może pozostać taka sama lub być zmieniona

        mockMvc.perform(put("/api/users/" + regularUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDetailsToUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("userIT_updated")))
                .andExpect(jsonPath("$.password").doesNotExist()); // Hasło nie powinno być zwracane
    }

    @Test
    @WithMockUser(username = "adminIT", roles = {"ADMIN"})
    void shouldDeleteUser_whenAdmin() throws Exception {
        mockMvc.perform(delete("/api/users/" + regularUser.getId()))
                .andExpect(status().isNoContent());

        // Weryfikacja, czy użytkownik został usunięty
        assertFalse(userRepository.findById(regularUser.getId()).isPresent());
    }

    @Test
    @WithMockUser(username = "userIT", roles = {"USER"})
    void shouldFailToDeleteUser_whenRegularUser() throws Exception {
        mockMvc.perform(delete("/api/users/" + adminUser.getId())) // Próba usunięcia innego użytkownika
                .andExpect(status().isForbidden());
    }
}