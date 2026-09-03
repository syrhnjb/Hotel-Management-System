package com.abhiram.hotel.exception;

/** Thrown when a room number does not correspond to any room in the hotel. */
public class RoomNotFoundException extends HotelDomainException {

    private static final long serialVersionUID = 1L;

    public RoomNotFoundException(int roomNumber) {
        super("Room " + roomNumber + " does not exist");
    }
}
