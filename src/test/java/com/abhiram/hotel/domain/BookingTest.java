package com.abhiram.hotel.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.abhiram.hotel.exception.InvalidBookingException;
import java.util.List;
import org.junit.jupiter.api.Test;

class BookingTest {

    private Guest guest(String name) {
        return new Guest(name, ContactNumber.of("9876543210"), "F");
    }

    @Test
    void singleRoomWithExactlyOneGuestSucceeds() {
        Booking booking = new Booking(31, RoomType.LUXURY_SINGLE, List.of(guest("Asha")));
        assertEquals(1, booking.guests().size());
        assertTrue(booking.isActive());
    }

    @Test
    void doubleRoomRequiresExactlyTwoGuests() {
        assertThrows(InvalidBookingException.class,
                () -> new Booking(1, RoomType.LUXURY_DOUBLE, List.of(guest("Asha"))));
        assertThrows(InvalidBookingException.class,
                () -> new Booking(1, RoomType.LUXURY_DOUBLE,
                        List.of(guest("Asha"), guest("Ravi"), guest("Extra"))));
    }

    @Test
    void markCheckedOutTwiceThrows() {
        Booking booking = new Booking(31, RoomType.LUXURY_SINGLE, List.of(guest("Asha")));
        booking.markCheckedOut();

        assertThrows(InvalidBookingException.class, booking::markCheckedOut);
    }
}
