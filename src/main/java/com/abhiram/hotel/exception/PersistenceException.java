package com.abhiram.hotel.exception;

/** Thrown when saving or loading hotel state to/from disk fails. */
public class PersistenceException extends HotelDomainException {

    private static final long serialVersionUID = 1L;

    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }

    public PersistenceException(String message) {
        super(message);
    }
}
