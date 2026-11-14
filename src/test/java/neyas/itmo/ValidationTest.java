package neyas.itmo;

import neyas.itmo.exception.DomainValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ValidationTest {

    @Test
    void requireNotNullReturnsValue() {
        assertEquals("значение", Validation.requireNotNull("значение", "поле"));
    }

    @Test
    void requireNotNullRejectsNull() {
        assertThrows(DomainValidationException.class, () -> Validation.requireNotNull(null, "поле"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\t"})
    void requireNotBlankRejectsEmptyStrings(String value) {
        assertThrows(DomainValidationException.class, () -> Validation.requireNotBlank(value, "поле"));
    }

    @Test
    void requireNotEmptyRejectsEmptyCollection() {
        assertThrows(DomainValidationException.class, () -> Validation.requireNotEmpty(Set.of(), "коллекция"));
        assertDoesNotThrow(() -> Validation.requireNotEmpty(List.of(1), "коллекция"));
    }

    @Test
    void requirePositiveRejectsZeroAndNegative() {
        assertEquals(5L, Validation.requirePositive(5L, "цена"));
        assertThrows(DomainValidationException.class, () -> Validation.requirePositive(0L, "цена"));
        assertThrows(DomainValidationException.class, () -> Validation.requirePositive(-1L, "цена"));
    }

    @Test
    void requirePositiveOrZeroAllowsZero() {
        assertEquals(0L, Validation.requirePositiveOrZero(0L, "остаток"));
        assertThrows(DomainValidationException.class, () -> Validation.requirePositiveOrZero(-1L, "остаток"));
        assertThrows(DomainValidationException.class, () -> Validation.requirePositiveOrZero(-0.5, "объем"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"client@mail.ru", "a.b+c@sub.example.com"})
    void acceptsValidMail(String mail) {
        assertEquals(mail, Validation.isAvailableMailFormat(mail));
    }

    @ParameterizedTest
    @ValueSource(strings = {"client", "client@", "@mail.ru", "client@mail"})
    void rejectsInvalidMail(String mail) {
        assertThrows(DomainValidationException.class, () -> Validation.isAvailableMailFormat(mail));
    }

    @Test
    void checksNumberFormat() {
        assertEquals("+79990000000", Validation.isAvailableNumberFormat("+79990000000"));
        assertThrows(DomainValidationException.class, () -> Validation.isAvailableNumberFormat("89990000000"));
        assertThrows(DomainValidationException.class, () -> Validation.isAvailableNumberFormat("+7999"));
    }

    @Test
    void requireThrowsWithGivenMessage() {
        DomainValidationException exception = assertThrows(DomainValidationException.class,
                () -> Validation.require(false, "нельзя"));
        assertEquals("нельзя", exception.getMessage());
    }
}
