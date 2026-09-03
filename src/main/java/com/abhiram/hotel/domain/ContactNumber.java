package com.abhiram.hotel.domain;

import com.abhiram.hotel.exception.ValidationException;

/**
 * Value object representing a guest's contact number.
 *
 * <p>Accepts 7-15 digits, optionally prefixed with {@code +}. This is intentionally permissive
 * (it does not attempt full international phone-number validation) since the domain only needs
 * "looks like a phone number" confidence, not carrier-level verification.
 */
public final class ContactNumber {

    private static final String PATTERN = "^\\+?[0-9]{7,15}$";

    private final String value;

    private ContactNumber(String value) {
        this.value = value;
    }

    public static ContactNumber of(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            throw new ValidationException("Contact number must not be empty");
        }
        String trimmed = rawValue.trim();
        if (!trimmed.matches(PATTERN)) {
            throw new ValidationException(
                    "Contact number must be 7-15 digits (optionally starting with '+'): " + rawValue);
        }
        return new ContactNumber(trimmed);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ContactNumber)) return false;
        return value.equals(((ContactNumber) o).value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}
