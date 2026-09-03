package com.abhiram.hotel.validation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.abhiram.hotel.domain.ContactNumber;
import com.abhiram.hotel.exception.ValidationException;
import org.junit.jupiter.api.Test;

class ContactNumberTest {

    @Test
    void plainDigitsAreAccepted() {
        assertDoesNotThrow(() -> ContactNumber.of("9876543210"));
    }

    @Test
    void leadingPlusIsAccepted() {
        assertDoesNotThrow(() -> ContactNumber.of("+919876543210"));
    }

    @Test
    void tooShortIsRejected() {
        assertThrows(ValidationException.class, () -> ContactNumber.of("12345"));
    }

    @Test
    void nonNumericIsRejected() {
        assertThrows(ValidationException.class, () -> ContactNumber.of("abc1234567"));
    }

    @Test
    void blankIsRejected() {
        assertThrows(ValidationException.class, () -> ContactNumber.of("   "));
        assertThrows(ValidationException.class, () -> ContactNumber.of(null));
    }
}
