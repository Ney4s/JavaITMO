package neyas.itmo;

import neyas.itmo.exception.DomainValidationException;

import java.util.Collection;
import java.util.regex.Pattern;

public final class Validation {

    private static final Pattern MAIL_PATTERN = Pattern.compile("^[\\w.+-]+@([\\w-]+\\.)+[a-zA-Z]{2,}$");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("^\\+\\d{10,15}$");

    private Validation() {}

    public static <T> T requireNotNull(T value, String message) {
        if (value == null) {
            throw new DomainValidationException("Значение не может быть null: %s".formatted(message));
        }
        return value;
    }

    public static String isAvailableMailFormat(String mailFormat) {
        requireNotBlank(mailFormat, "почта");
        require(MAIL_PATTERN.matcher(mailFormat).matches(),
                "Почта имеет неверный формат: %s".formatted(mailFormat));
        return mailFormat;
    }

    public static String isAvailableNumberFormat(String numberFormat) {
        requireNotBlank(numberFormat, "номер телефона");
        require(NUMBER_PATTERN.matcher(numberFormat).matches(),
                "Номер телефона имеет неверный формат: %s".formatted(numberFormat));
        return numberFormat;
    }

    public static void require(boolean condition, String message) {
        if (!condition) {
            throw new DomainValidationException(message);
        }
    }

    public static String requireNotBlank(String value, String message) {
        requireNotNull(value, message);
        if (value.isBlank()) {
            throw new DomainValidationException("Значение не может быть пустым: %s".formatted(message));
        }
        return value;
    }

    public static <T> Collection<T> requireNotEmpty(Collection<T> value, String message) {
        requireNotNull(value, message);
        if (value.isEmpty()) {
            throw new DomainValidationException("Коллекция не может быть пустой: %s".formatted(message));
        }
        return value;
    }

    public static long requirePositive(long value, String message) {
        require(value > 0, "%s должно быть больше нуля, получено %d".formatted(message, value));
        return value;
    }

    public static long requirePositiveOrZero(long value, String message) {
        require(value >= 0, "%s не может быть отрицательным, получено %d".formatted(message, value));
        return value;
    }

    public static double requirePositiveOrZero(double value, String message) {
        require(value >= 0, "%s не может быть отрицательным, получено %s".formatted(message, value));
        return value;
    }
}
