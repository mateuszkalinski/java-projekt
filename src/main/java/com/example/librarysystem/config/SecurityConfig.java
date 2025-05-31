package com.example.librarysystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity // Włącza obsługę Spring Security
public class SecurityConfig {

    // Bean do hashowania haseł
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Bean konfigurujący łańcuch filtrów bezpieczeństwa
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Na razie wyłączymy CSRF, co jest częste dla REST API,
                // ale w aplikacji webowej z formularzami warto to przemyśleć.
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authz -> authz
                                // Na tym etapie możemy zezwolić na dostęp do wszystkich endpointów
                                // lub zdefiniować bardzo proste reguły.
                                // Zezwólmy na razie na wszystko, aby móc testować bez logowania.
                                // Później będziemy to zawężać.
                                .requestMatchers("/**").permitAll() // UWAGA: To jest tylko na czas developmentu!
                        // Przykładowe reguły, które dodamy później:
                        // .requestMatchers("/api/auth/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll() // Publiczne endpointy
                        // .requestMatchers("/api/admin/**").hasRole("ADMIN") // Endpointy dla admina
                        // .anyRequest().authenticated() // Wszystkie inne żądania wymagają uwierzytelnienia
                )
                // Możemy dodać konfigurację formularza logowania lub HTTP Basic
                .httpBasic(org.springframework.security.config.Customizer.withDefaults()); // Proste logowanie HTTP Basic na start

        return http.build();
    }
}