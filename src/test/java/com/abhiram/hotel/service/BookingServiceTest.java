package com.abhiram.hotel.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.abhiram.hotel.domain.Booking;
import com.abhiram.hotel.domain.ContactNumber;
import com.abhiram.hotel.domain.Guest;
import com.abhiram.hotel.domain.Invoice;
import com.abhiram.hotel.domain.RoomType;
import com.abhiram.hotel.exception.BookingNotFoundException;
import com.abhiram.hotel.exception.InvalidBookingException;
import com.abhiram.hotel.exception.RoomNotAvailableException;
import com.abhiram.hotel.exception.RoomNotFoundException;
import com.abhiram.hotel.repository.BookingRepository;
import com.abhiram.hotel.repository.InMemoryBookingRepository;
import com.abhiram.hotel.repository.InMemoryRoomRepository;
import com.abhiram.hotel.repository.RoomRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BookingServiceTest {

    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        RoomRepository roomRepository = new InMemoryRoomRepository();
        BookingRepository bookingRepository = new InMemoryBookingRepository();
        bookingService = new BookingService(roomRepository, bookingRepository, new BillingService());
    }

    private Guest guest(String name) {
        return new Guest(name, ContactNumber.of("9876543210"), "F");
    }

    private List<Guest> singleGuest() {
        return List.of(guest("Asha"));
    }

    private List<Guest> doubleGuests() {
        return List.of(guest("Asha"), guest("Ravi"));
    }

    @Test
    void booksAnAvailableSingleRoom() {
        Booking booking = bookingService.bookRoom(31, singleGuest());

        assertEquals(31, booking.roomNumber());
        assertEquals(RoomType.LUXURY_SINGLE, booking.roomType());
        assertTrue(booking.isActive());
    }

    @Test
    void bookingAnAlreadyOccupiedRoomThrows() {
        bookingService.bookRoom(31, singleGuest());

        assertThrows(RoomNotAvailableException.class, () -> bookingService.bookRoom(31, singleGuest()));
    }

    @Test
    void bookingANonExistentRoomThrows() {
        assertThrows(RoomNotFoundException.class, () -> bookingService.bookRoom(999, singleGuest()));
        assertThrows(RoomNotFoundException.class, () -> bookingService.bookRoom(0, singleGuest()));
    }

    @Test
    void bookingADoubleRoomWithOnlyOneGuestThrows() {
        assertThrows(InvalidBookingException.class, () -> bookingService.bookRoom(1, singleGuest()));
    }

    @Test
    void bookingASingleRoomWithTwoGuestsThrows() {
        assertThrows(InvalidBookingException.class, () -> bookingService.bookRoom(31, doubleGuests()));
    }

    @Test
    void rejectedBookingDoesNotOccupyTheRoom() {
        try {
            bookingService.bookRoom(31, doubleGuests());
        } catch (InvalidBookingException ignored) {
            // expected
        }
        // Room should still be bookable, proving the failed attempt left no partial state.
        Booking booking = bookingService.bookRoom(31, singleGuest());
        assertTrue(booking.isActive());
    }

    @Test
    void checkoutOfUnbookedRoomThrows() {
        assertThrows(BookingNotFoundException.class, () -> bookingService.checkout(31));
    }

    @Test
    void checkoutReleasesTheRoomForRebooking() {
        bookingService.bookRoom(31, singleGuest());

        Invoice invoice = bookingService.checkout(31);

        assertEquals(31, invoice.roomNumber());
        assertFalse(bookingService.allActiveBookings().stream()
                .anyMatch(b -> b.roomNumber() == 31));
        // Room is available again.
        Booking rebooked = bookingService.bookRoom(31, singleGuest());
        assertTrue(rebooked.isActive());
    }

    @Test
    void doubleCheckoutOfTheSameRoomThrowsTheSecondTime() {
        bookingService.bookRoom(31, singleGuest());
        bookingService.checkout(31);

        assertThrows(BookingNotFoundException.class, () -> bookingService.checkout(31));
    }
}
