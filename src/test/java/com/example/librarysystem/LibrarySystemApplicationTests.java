package com.example.librarysystem;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
// Importuj tylko to, co jest potrzebne, jeśli testujesz konkretne profile
// import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
// @ActiveProfiles("test") // Jeśli masz profil 'test' skonfigurowany np. z H2
class LibrarySystemApplicationTests {

    @Test
    void contextLoads() {
        // Ten test przejdzie, jeśli kontekst aplikacji załaduje się bez błędów
    }
}