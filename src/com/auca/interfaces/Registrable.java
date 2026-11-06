package com.auca.interfaces;

import com.auca.exceptions.ValidationException;
import java.math.BigDecimal;
import java.time.LocalDate;

public interface Registrable {
    int MIN_NAME_LENGTH = 2;
    int MAX_NAME_LENGTH = 60;
    int MAX_EMAIL_LENGTH = 100;
    int MIN_PHONE_LENGTH = 10;
    int MAX_PHONE_LENGTH = 15;
    String DATE_PATTERN = "yyyy-MM-dd";

    void validate() throws ValidationException;

    void printDetails();

    String getRegistrationSummary();


    static String clean(String value) {
        return value == null ? null : value.trim();
    }

    static void requireNotNull(String label, Object value) throws ValidationException {
        if (value == null) {
            throw new ValidationException(label + " is required.");
        }
    }

    static void requireLength(String label, String value, int min, int max) throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(label + " cannot be blank.");
        }
        int length = value.trim().length();
        if (length < min || length > max) {
            throw new ValidationException(label + " must be " + min + "-" + max + " characters.");
        }
    }

    static void requireEmail(String label, String value) throws ValidationException {
        requireLength(label, value, 5, MAX_EMAIL_LENGTH);
        String email = value.trim();
        int at = email.indexOf('@');
        int dot = email.lastIndexOf('.');
        if (at <= 0 || at != email.lastIndexOf('@') || dot < at + 2
                || dot > email.length() - 2 || email.contains(" ")) {
            throw new ValidationException(label + " must be a valid email (example: name@mail.com).");
        }
    }

    static void requirePhone(String label, String value) throws ValidationException {
        String phone = value == null ? "" : value.trim();
        String digits = phone.startsWith("+") ? phone.substring(1) : phone;
        boolean valid = digits.length() >= MIN_PHONE_LENGTH && digits.length() <= MAX_PHONE_LENGTH;
        for (int i = 0; i < digits.length(); i++) {
            if (!Character.isDigit(digits.charAt(i))) {
                valid = false;
            }
        }
        if (!valid) {
            throw new ValidationException(label + " must contain " + MIN_PHONE_LENGTH + "-"
                    + MAX_PHONE_LENGTH + " digits with an optional leading +.");
        }
    }

    static void requireRange(String label, int value, int min, int max) throws ValidationException {
        if (value < min || value > max) {
            throw new ValidationException(label + " must be between " + min + " and " + max + ".");
        }
    }

    static void requireMin(String label, int value, int min) throws ValidationException {
        if (value < min) {
            throw new ValidationException(label + " must be at least " + min + ".");
        }
    }

    static void requirePositive(String label, BigDecimal value) throws ValidationException {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException(label + " must be greater than 0.");
        }
    }

    static void requireNotFuture(String label, LocalDate date) throws ValidationException {
        requireNotNull(label, date);
        if (date.isAfter(LocalDate.now())) {
            throw new ValidationException(label + " cannot be in the future.");
        }
    }

    static void requirePast(String label, LocalDate date) throws ValidationException {
        requireNotNull(label, date);
        if (!date.isBefore(LocalDate.now())) {
            throw new ValidationException(label + " must be in the past.");
        }
    }

    static void requireOneOf(String label, String value, String[] allowed) throws ValidationException {
        for (int i = 0; i < allowed.length; i++) {
            if (allowed[i].equals(value)) {
                return;
            }
        }
        throw new ValidationException(label + " must be one of: " + String.join(", ", allowed) + ".");
    }
}