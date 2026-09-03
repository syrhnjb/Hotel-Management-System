package com.abhiram.hotel.exception;

/** Thrown when an operation (food order, checkout) targets a room with no active booking. */
public class BookingNotFoundException extends HotelDomainException {

    private static final long serialVersionUID = 1L;

    public BookingNotFoundException(int roomNumber) {
        super("Room " + roomNumber + " has no active booking");
    }
}
