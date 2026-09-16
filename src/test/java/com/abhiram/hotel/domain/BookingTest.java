package com.abhiram.hotel.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.abhiram.hotel.exception.InvalidBookingException;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class BookingTest {

    private final LocalDate today = LocalDate.now();

    private Guest guest(String name) {
        return new Guest(name, ContactNumber.of("9876543210"), "F");
    }

    @Test
    void singleRoomWithExactlyOneGuestSucceeds() {
        Booking booking = new Booking(31, RoomType.LUXURY_SINGLE, List.of(guest("Asha")),
                today, today.plusDays(1));
        assertEquals(1, booking.guests().size());
        assertTrue(booking.isActive());
    }

    @Test
    void doubleRoomRequiresExactlyTwoGuests() {
        assertThrows(InvalidBookingException.class,
                () -> new Booking(1, RoomType.LUXURY_DOUBLE, List.of(guest("Asha")),
                        today, today.plusDays(1)));
        assertThrows(InvalidBookingException.class,
                () -> new Booking(1, RoomType.LUXURY_DOUBLE,
                        List.of(guest("Asha"), guest("Ravi"), guest("Extra")),
                        today, today.plusDays(1)));
    }

    @Test
    void markCheckedOutTwiceThrows() {
        Booking booking = new Booking(31, RoomType.LUXURY_SINGLE, List.of(guest("Asha")),
                today, today.plusDays(1));
        booking.markCheckedOut();

        assertThrows(InvalidBookingException.class, booking::markCheckedOut);
    }

    @Test
    void nightsAreTheDaysBetweenCheckInAndCheckOut() {
        Booking booking = new Booking(31, RoomType.LUXURY_SINGLE, List.of(guest("Asha")),
                today, today.plusDays(4));

        assertEquals(4, booking.nights());
        assertTrue(booking.covers(today));
        assertTrue(booking.covers(today.plusDays(3)));
        assertTrue(!booking.covers(today.plusDays(4)));
    }

    @Test
    void futureStayIsReservedUntilCheckIn() {
        Booking booking = new Booking(31, RoomType.LUXURY_SINGLE, List.of(guest("Asha")),
                today.plusDays(2), today.plusDays(4));

        assertTrue(booking.isReserved());
        assertTrue(!booking.isInHouse());
        assertTrue(booking.isOpen());
    }

    @Test
    void checkOutMustBeAfterCheckIn() {
        assertThrows(InvalidBookingException.class,
                () -> new Booking(31, RoomType.LUXURY_SINGLE, List.of(guest("Asha")),
                        today, today));
        assertThrows(InvalidBookingException.class,
                () -> new Booking(31, RoomType.LUXURY_SINGLE, List.of(guest("Asha")),
                        today.plusDays(2), today.plusDays(1)));
    }
}