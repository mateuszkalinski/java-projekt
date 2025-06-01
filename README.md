# System Zarządzania Biblioteką (Library System)

Nowoczesny system do zarządzania zasobami bibliotecznymi i wypożyczeniami, zbudowany z wykorzystaniem Spring Boot, Spring Security, JPA/Hibernate oraz PostgreSQL, konteneryzowany przy użyciu Docker.

---

## 📋 Spis treści

1.  [🎯 Opis projektu](#opis-projektu)
2.  [✨ Kluczowe funkcjonalności](#kluczowe-funkcjonalnosci)
3.  [🏗️ Architektura i wzorce projektowe](#architektura-i-wzorce-projektowe)
    * [Wzorce projektowe](#wzorce-projektowe)
    * [Polimorfizm](#polimorfizm)
4.  [🔐 System autoryzacji (RBAC)](#system-autoryzacji-rbac)
    * [Role użytkowników](#role-uzytkownikow)
    * [Implementacja bezpieczeństwa](#implementacja-bezpieczenstwa)
5.  [🚀 Technologie](#technologie)
6.  [🐳 Szybki start z Docker](#szybki-start-z-docker)
7.  [📚 Dokumentacja API (Swagger)](#dokumentacja-api-swagger)
8.  [🗄️ Model bazy danych (ERD)](#model-bazy-danych-erd)
9.  [🔧 Konfiguracja](#konfiguracja)
10. [🧪 Testowanie](#testowanie)
11. [📸 Zrzuty ekranu](#zrzuty-ekranu) (do uzupełnienia)

---

## 🎯 Opis projektu

**System Zarządzania Biblioteką** to aplikacja webowa umożliwiająca efektywne zarządzanie zbiorami książek, procesem ich wypożyczania oraz użytkownikami systemu. Aplikacja została zaprojektowana z myślą o modularności, bezpieczeństwie i łatwości obsługi, zgodnie z zasadami programowania obiektowego i SOLID.

---

## ✨ Kluczowe funkcjonalności

* **👤 Zarządzanie użytkownikami:**
    * Rejestracja nowych użytkowników.
    * Logowanie użytkowników.
    * System ról (Użytkownik, Administrator).
* **📚 Katalog książek:**
    * Dodawanie, edycja, usuwanie książek (Admin).
    * Przeglądanie i wyszukiwanie książek (Wszyscy zalogowani).
* **🎫 Zarządzanie wypożyczeniami:**
    * Wypożyczanie książek przez użytkowników.
    * Zwracanie książek.
    * Przeglądanie historii wypożyczeń (własnych dla User, wszystkich dla Admin).

---

## 🏗️ Architektura i wzorce projektowe

Projekt został zbudowany zgodnie z **filarami obiektowości** i **zasadami SOLID**. Zastosowano standardową architekturę warstwową typową dla aplikacji Spring Boot (Kontrolery, Serwisy, Repozytoria).

### Wzorce projektowe

1.  **Wzorzec Strategia (Strategy Pattern)** 🎯
    * **Lokalizacja:** `com.example.librarysystem.service.policy.LoanPolicy` (interfejs) oraz jego implementacje `StandardLoanPolicy` i `AcademicLoanPolicy`.
    * **Opis:** Wzorzec Strategia został użyty do zdefiniowania różnych polityk obliczania terminu zwrotu książki. `BorrowingService` korzysta z wstrzykniętej implementacji `LoanPolicy` do elastycznego określania daty zwrotu, co pozwala na łatwe dodawanie nowych polityk wypożyczeń bez modyfikacji samego serwisu.

    ```java
    // Interfejs LoanPolicy
    public interface LoanPolicy {
        LocalDate calculateDueDate(LocalDate borrowDate, Book book, User user);
    }

    // Implementacja StandardLoanPolicy
    @Component("standardLoanPolicy")
    public class StandardLoanPolicy implements LoanPolicy {
        private static final int STANDARD_LOAN_DURATION_DAYS = 14;
        @Override
        public LocalDate calculateDueDate(LocalDate borrowDate, Book book, User user) {
            return borrowDate.plusDays(STANDARD_LOAN_DURATION_DAYS);
        }
    }
    ```

### Polimorfizm

System wykorzystuje **polimorfizm** poprzez interfejs `LoanPolicy`. Serwis `BorrowingService` operuje na abstrakcji `LoanPolicy`, a konkretne zachowanie (sposób obliczania daty zwrotu) jest determinowane przez rzeczywisty typ obiektu (np. `StandardLoanPolicy`, `AcademicLoanPolicy`) wstrzyknięty w czasie działania aplikacji.

```java
// W BorrowingService
private final LoanPolicy loanPolicy;

public BorrowingService(/*...*/, @Qualifier("standardLoanPolicy") LoanPolicy loanPolicy) {
    this.loanPolicy = loanPolicy;
}

public Borrowing borrowBook(Long userId, Long bookId) {
    // ...
    LocalDate dueDate = this.loanPolicy.calculateDueDate(borrowDate, book, user); // Wykorzystanie polimorfizmu
    // ...
}
