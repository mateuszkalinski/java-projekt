package com.example.librarysystem;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) // Można użyć RANDOM_PORT lub MOCK
@Testcontainers // Włącza obsługę Testcontainers
class LibrarySystemApplicationTests {

    // Definicja kontenera PostgreSQL - taka sama jak w innych testach IT
    @Container
    public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("test_db_context_load") // Można dać inną nazwę dla tego testu
            .withUsername("testuser")
            .withPassword("testpass");

    // Dynamiczne ustawienie właściwości Springa, aby wskazywały na kontener Testcontainers
    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        // Dla testu ładowania kontekstu, 'create-drop' jest OK,
        // Flyway może być wyłączony, jeśli nie testujemy migracji tutaj.
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        // Jeśli Flyway jest włączony i nie masz migracji, może to powodować ostrzeżenia lub błędy.
        // Dla samego testu ładowania kontekstu można go wyłączyć:
        registry.add("spring.flyway.enabled", () -> false);
    }

    @Test
    void contextLoads() {
        // Ten test przejdzie, jeśli kontekst aplikacji załaduje się poprawnie
        // z użyciem bazy danych PostgreSQL dostarczonej przez Testcontainers.
    }
}