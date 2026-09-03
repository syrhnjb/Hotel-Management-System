package com.abhiram.hotel.validation;

import com.abhiram.hotel.exception.ValidationException;

/**
 * Small, focused input-validation helpers shared by the service and CLI layers.
 *
 * <p>Kept as static utility methods rather than an injected object: these checks are pure,
 * stateless, and have no dependencies, so an instance would add ceremony without benefit.
 */
public final class Validator {

    private Validator() {
    }

    public static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(fieldName + " must not be empty");
        }
    }

    public static void requirePositiveQuantity(int quantity, String fieldName) {
        if (quantity <= 0) {
            throw new ValidationException(fieldName + " must be a positive number, got: " + quantity);
        }
    }

    public static void requireInRange(int value, int min, int max, String fieldName) {
        if (value < min || value > max) {
            throw new ValidationException(
                    fieldName + " must be between " + min + " and " + max + ", got: " + value);
        }
    }
}
