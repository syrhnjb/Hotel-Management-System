package com.abhiram.hotel.exception;

/** Thrown when a food order line is invalid: bad quantity, unknown item, and similar. */
public class InvalidFoodOrderException extends HotelDomainException {

    private static final long serialVersionUID = 1L;

    public InvalidFoodOrderException(String message) {
        super(message);
    }
}
