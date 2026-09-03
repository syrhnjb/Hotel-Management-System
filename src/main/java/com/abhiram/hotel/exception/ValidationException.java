package com.abhiram.hotel.exception;

/** Thrown when user-supplied input fails validation (empty name, malformed contact number, etc). */
public class ValidationException extends HotelDomainException {

    private static final long serialVersionUID = 1L;

    public ValidationException(String message) {
        super(message);
    }
}
