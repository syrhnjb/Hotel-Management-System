package com.abhiram.hotel.validation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.abhiram.hotel.exception.ValidationException;
import org.junit.jupiter.api.Test;

class ValidatorTest {

    @Test
    void blankNameIsRejected() {
        assertThrows(ValidationException.class, () -> Validator.requireNonBlank("  ", "Name"));
        assertThrows(ValidationException.class, () -> Validator.requireNonBlank(null, "Name"));
    }

    @Test
    void nonBlankNameIsAccepted() {
        assertDoesNotThrow(() -> Validator.requireNonBlank("Asha", "Name"));
    }

    @Test
    void zeroAndNegativeQuantitiesAreRejected() {
        assertThrows(ValidationException.class, () -> Validator.requirePositiveQuantity(0, "Quantity"));
        assertThrows(ValidationException.class, () -> Validator.requirePositiveQuantity(-5, "Quantity"));
    }

    @Test
    void valueOutsideRangeIsRejected() {
        assertThrows(ValidationException.class, () -> Validator.requireInRange(70, 1, 60, "Room number"));
    }

    @Test
    void boundaryValuesAreAccepted() {
        assertDoesNotThrow(() -> Validator.requireInRange(1, 1, 60, "Room number"));
        assertDoesNotThrow(() -> Validator.requireInRange(60, 1, 60, "Room number"));
    }
}
