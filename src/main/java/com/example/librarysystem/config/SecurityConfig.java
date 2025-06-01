package com.example.librarysystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/api/users/register").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()

                        // GET /api/books i GET /api/books/{co‐kolwiek} – OK
                        .requestMatchers(HttpMethod.GET, "/api/books", "/api/books/**").hasAnyRole("USER", "ADMIN")

                        // Dwa osobne endpointy: /api/borrowings/borrow  oraz  /api/borrowings/{id}/return
                        .requestMatchers("/api/borrowings/borrow").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/borrowings/*/return").hasAnyRole("USER", "ADMIN")

                        // GET /api/borrowings/user/{co‐kolwiek} – OK, bo * to odpowiada jednemu poziomowi ścieżki
                        .requestMatchers("/api/borrowings/user/**").hasAnyRole("USER", "ADMIN")

                        // Dla ADMINa: tworzenie, edycja, usuwanie książek
                        .requestMatchers(HttpMethod.POST, "/api/books").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/books/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/books/**").hasRole("ADMIN")

                        // Dla ADMINa wszelkie operacje na użytkownikach
                        .requestMatchers("/api/users/**").hasRole("ADMIN")

                        // Dla ADMINa lista wszystkich wypożyczeń "/api/borrowings"
                        .requestMatchers("/api/borrowings").hasRole("ADMIN")

                        // pozostałe żądania – uwierzytelnienie
                        .anyRequest().authenticated()
                )
                .formLogin(formLogin -> formLogin
                        // Domyślny formularz
                        .defaultSuccessUrl("/swagger-ui.html", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/?logout=true")
                        .permitAll()
                );
        return http.build();
    }
}
