package com.example.librarysystem.integration;

import com.example.librarysystem.entity.User;
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

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
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
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User adminUser;
    private User regularUser;

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

        userRepository.deleteAll();

        adminUser = new User();
        adminUser.setUsername("adminIT");
        adminUser.setPassword(passwordEncoder.encode("password"));
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
        // Budujemy JSON ręcznie, aby pole "password" znalazło się w żądaniu
        String userJson = """
            {
                "username": "newUserRegister",
                "password": "plainPassword123",
                "role": "ROLE_USER"
            }
            """;

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(notNullValue())))
                .andExpect(jsonPath("$.username", is("newUserRegister")))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @WithMockUser(username = "adminIT", roles = {"ADMIN"})
    void shouldGetAllUsers_whenAdmin() throws Exception {
        mockMvc.perform(get("/api/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].username").exists())
                .andExpect(jsonPath("$[0].password").doesNotExist())
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

    @Test
    @WithMockUser(username = "adminIT", roles = {"ADMIN"})
    void shouldUpdateUser_whenAdmin() throws Exception {
        User userDetailsToUpdate = new User();
        userDetailsToUpdate.setUsername("userIT_updated");
        // Jeśli chcemy zaktualizować hasło, należy je tu podać:
        // userDetailsToUpdate.setPassword("newPlainPassword");
        userDetailsToUpdate.setRole("ROLE_USER");

        // Serializacja obiektu bez pola "password" jest ok w tym przypadku,
        // bo w teście nie zmieniamy hasła.
        String updateJson = objectMapper.writeValueAsString(userDetailsToUpdate);

        mockMvc.perform(put("/api/users/" + regularUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("userIT_updated")))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @WithMockUser(username = "adminIT", roles = {"ADMIN"})
    void shouldDeleteUser_whenAdmin() throws Exception {
        mockMvc.perform(delete("/api/users/" + regularUser.getId()))
                .andExpect(status().isNoContent());

        assertFalse(userRepository.findById(regularUser.getId()).isPresent());
    }

    @Test
    @WithMockUser(username = "userIT", roles = {"USER"})
    void shouldFailToDeleteUser_whenRegularUser() throws Exception {
        mockMvc.perform(delete("/api/users/" + adminUser.getId()))
                .andExpect(status().isForbidden());
    }
}
