package com.example.librarysystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod; // <--- NOWY IMPORT
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher; // <--- NOWY IMPORT
import static org.springframework.security.config.Customizer.withDefaults; // Dla httpBasic lub formLogin z domyślnymi

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
                        .requestMatchers(HttpMethod.GET, "/api/books", "/api/books/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/borrowings/borrow", "/api/borrowings/**/return").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/borrowings/user/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/books").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/books/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/books/**").hasRole("ADMIN")
                        .requestMatchers("/api/users/**").hasRole("ADMIN") // Pamiętaj, że GET /api/users/{id} może być dla usera samego siebie
                        .requestMatchers("/api/borrowings").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(formLogin -> formLogin // Używamy domyślnego formularza logowania Springa
                        // .loginPage("/my-custom-login") // Usunięte lub zakomentowane, jeśli nie masz własnej strony
                        .defaultSuccessUrl("/", true) // Przekieruj na główną ścieżkę po sukcesie
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/?logout=true") // Przekieruj na główną ścieżkę z info o wylogowaniu
                        .permitAll()
                );
        // Jeśli chcesz umożliwić testowanie API np. przez Postmana bez formularza logowania,
        // a jedynie przez podstawowe uwierzytelnianie HTTP, możesz zostawić lub dodać:
        // .httpBasic(withDefaults());
        // Jednak formLogin jest bardziej typowe dla interakcji przez przeglądarkę.

        return http.build();
    }
}