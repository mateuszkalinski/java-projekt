package com.example.librarysystem.service;

import com.example.librarysystem.entity.User;
import com.example.librarysystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder; // <--- Mock dla PasswordEncoder

    @InjectMocks
    private UserService userService;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setId(1L);
        user1.setUsername("testUser1");
        user1.setPassword("hashedPassword1"); // Zakładamy, że hasło jest już zahashowane w mocku
        user1.setRole("ROLE_USER");

        user2 = new User();
        user2.setId(2L);
        user2.setUsername("adminUser");
        user2.setPassword("hashedPasswordAdmin");
        user2.setRole("ROLE_ADMIN");
    }

    @Test
    @DisplayName("loadUserByUsername - powinien zwrócić UserDetails, gdy użytkownik istnieje")
    void testLoadUserByUsername_whenUserExists() {
        when(userRepository.findByUsername("testUser1")).thenReturn(Optional.of(user1));

        UserDetails userDetails = userService.loadUserByUsername("testUser1");

        assertNotNull(userDetails);
        assertEquals(user1.getUsername(), userDetails.getUsername());
        assertEquals(user1.getPassword(), userDetails.getPassword()); // Sprawdzamy zahashowane hasło
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_USER")));
        verify(userRepository, times(1)).findByUsername("testUser1");
    }

    @Test
    @DisplayName("loadUserByUsername - powinien rzucić UsernameNotFoundException, gdy użytkownik nie istnieje")
    void testLoadUserByUsername_whenUserDoesNotExist() {
        when(userRepository.findByUsername("nonExistentUser")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            userService.loadUserByUsername("nonExistentUser");
        });
        verify(userRepository, times(1)).findByUsername("nonExistentUser");
    }

    @Test
    @DisplayName("Powinien zwrócić wszystkich użytkowników")
    void testGetAllUsers() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));

        List<User> users = userService.getAllUsers();

        assertNotNull(users);
        assertEquals(2, users.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Powinien zwrócić użytkownika po ID, gdy użytkownik istnieje")
    void testGetUserById_whenUserExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));

        Optional<User> foundUserOptional = userService.getUserById(1L);

        assertTrue(foundUserOptional.isPresent());
        assertEquals(user1.getUsername(), foundUserOptional.get().getUsername());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("createUser - powinien zahashować hasło i zapisać użytkownika")
    void testCreateUser_shouldHashPasswordAndSaveUser() {
        User newUser = new User();
        newUser.setUsername("newUser");
        newUser.setPassword("plainPassword");
        newUser.setRole("USER"); // Rola bez prefiksu, serwis powinien go dodać

        String hashedPassword = "hashedNewPassword";
        when(userRepository.findByUsername("newUser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("plainPassword")).thenReturn(hashedPassword);
        // Symulacja zapisu - zwracamy obiekt newUser, ale zakładamy, że serwis ustawi zahashowane hasło i ID
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User userToSave = invocation.getArgument(0);
            userToSave.setId(3L); // Symulacja nadania ID przez bazę
            return userToSave;
        });

        User createdUser = userService.createUser(newUser);

        assertNotNull(createdUser);
        assertEquals("newUser", createdUser.getUsername());
        assertEquals(hashedPassword, createdUser.getPassword()); // Sprawdzamy, czy hasło zostało zahashowane
        assertEquals("ROLE_USER", createdUser.getRole()); // Sprawdzamy, czy rola została znormalizowana
        assertNotNull(createdUser.getId());

        verify(userRepository, times(1)).findByUsername("newUser");
        verify(passwordEncoder, times(1)).encode("plainPassword");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("createUser - powinien rzucić wyjątek, gdy nazwa użytkownika jest zajęta")
    void testCreateUser_whenUsernameIsTaken() {
        User existingUser = new User();
        existingUser.setUsername("existingUser");
        existingUser.setPassword("password");
        existingUser.setRole("ROLE_USER");

        when(userRepository.findByUsername("existingUser")).thenReturn(Optional.of(existingUser));

        User newUser = new User();
        newUser.setUsername("existingUser"); // Ta sama nazwa użytkownika
        newUser.setPassword("newPassword");
        newUser.setRole("ROLE_USER");

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            userService.createUser(newUser);
        });
        assertEquals("Username existingUser is already taken.", exception.getMessage());

        verify(userRepository, times(1)).findByUsername("existingUser");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }


    @Test
    @DisplayName("updateUser - powinien zaktualizować użytkownika i zahashować nowe hasło")
    void testUpdateUser_shouldUpdateAndHashNewPassword() {
        User userDetailsToUpdate = new User();
        userDetailsToUpdate.setUsername("updatedUser1"); // Nowa nazwa użytkownika
        userDetailsToUpdate.setPassword("newPlainPassword"); // Nowe hasło
        userDetailsToUpdate.setRole("ADMIN"); // Nowa rola bez prefixu

        String newHashedPassword = "newHashedPassword";

        when(userRepository.findById(1L)).thenReturn(Optional.of(user1)); // user1 to oryginalny user
        when(userRepository.findByUsername("updatedUser1")).thenReturn(Optional.empty()); // Nowa nazwa nie jest zajęta
        when(passwordEncoder.encode("newPlainPassword")).thenReturn(newHashedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updatedUser = userService.updateUser(1L, userDetailsToUpdate);

        assertNotNull(updatedUser);
        assertEquals("updatedUser1", updatedUser.getUsername());
        assertEquals(newHashedPassword, updatedUser.getPassword());
        assertEquals("ROLE_ADMIN", updatedUser.getRole()); // Sprawdzenie normalizacji roli
        assertEquals(1L, updatedUser.getId());

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findByUsername("updatedUser1");
        verify(passwordEncoder, times(1)).encode("newPlainPassword");
        verify(userRepository, times(1)).save(user1); // Sprawdzamy, czy to user1 (oryginalny obiekt) został zmodyfikowany i zapisany
    }


    @Test
    @DisplayName("updateUser - nie powinien zmieniać hasła, jeśli nie podano nowego")
    void testUpdateUser_shouldNotChangePasswordIfNotProvided() {
        User userDetailsToUpdate = new User();
        userDetailsToUpdate.setUsername("testUser1"); // Nazwa użytkownika bez zmian
        // Brak nowego hasła w userDetailsToUpdate (password jest null lub pusty)
        userDetailsToUpdate.setPassword(null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updatedUser = userService.updateUser(1L, userDetailsToUpdate);

        assertNotNull(updatedUser);
        assertEquals(user1.getPassword(), updatedUser.getPassword()); // Hasło powinno pozostać takie samo

        verify(passwordEncoder, never()).encode(anyString()); // Metoda encode nie powinna być wywołana
        verify(userRepository, times(1)).save(user1);
    }


    @Test
    @DisplayName("Powinien usunąć użytkownika po ID")
    void testDeleteUser_whenUserExists() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        assertDoesNotThrow(() -> userService.deleteUser(1L));

        verify(userRepository, times(1)).existsById(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }
}