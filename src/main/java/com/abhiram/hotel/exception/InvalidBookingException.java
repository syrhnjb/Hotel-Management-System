package com.abhiram.hotel.exception;

/** Thrown when booking data is internally inconsistent (e.g. wrong guest count for room type). */
public class InvalidBookingException extends HotelDomainException {

    private static final long serialVersionUID = 1L;

    public InvalidBookingException(String message) {
        super(message);
    }
}
