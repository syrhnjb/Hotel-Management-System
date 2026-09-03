package com.abhiram.hotel.exception;

/** Thrown when attempting to book a room that is already occupied. */
public class RoomNotAvailableException extends HotelDomainException {

    private static final long serialVersionUID = 1L;

    public RoomNotAvailableException(int roomNumber) {
        super("Room " + roomNumber + " is not available");
    }
}
