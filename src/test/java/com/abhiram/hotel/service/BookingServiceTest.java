package com.abhiram.hotel.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.abhiram.hotel.domain.Booking;
import com.abhiram.hotel.domain.ContactNumber;
import com.abhiram.hotel.domain.Guest;
import com.abhiram.hotel.domain.Invoice;
import com.abhiram.hotel.domain.Money;
import com.abhiram.hotel.domain.RoomType;
import com.abhiram.hotel.exception.BookingNotFoundException;
import com.abhiram.hotel.exception.InvalidBookingException;
import com.abhiram.hotel.exception.RoomNotAvailableException;
import com.abhiram.hotel.exception.RoomNotFoundException;
import com.abhiram.hotel.repository.BookingRepository;
import com.abhiram.hotel.repository.InMemoryBookingRepository;
import com.abhiram.hotel.repository.InMemoryInvoiceHistoryRepository;
import com.abhiram.hotel.repository.InMemoryRoomRepository;
import com.abhiram.hotel.repository.InvoiceHistoryRepository;
import com.abhiram.hotel.repository.RoomRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BookingServiceTest {

    private BookingService bookingService;
    private InvoiceHistoryRepository historyRepository;

    @BeforeEach
    void setUp() {
        RoomRepository roomRepository = new InMemoryRoomRepository();
        BookingRepository bookingRepository = new InMemoryBookingRepository();
        historyRepository = new InMemoryInvoiceHistoryRepository();
        bookingService = new BookingService(roomRepository, bookingRepository,
                new BillingService(), historyRepository);
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
        assertFalse(bookingService.openBookings().stream()
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

    // --- multi-day reservations ---

    @Test
    void booksForMultipleNightsAndBillsTheProratedRate() {
        LocalDate today = LocalDate.now();

        Booking booking = bookingService.bookRoom(31, singleGuest(), today, today.plusDays(3));
        assertEquals(3, booking.nights());

        Invoice invoice = bookingService.checkout(31);

        assertEquals(3, invoice.nights());
        assertEquals(Money.rupees(2200).multiply(3), invoice.roomCharge());
        assertEquals(Money.rupees(2200).multiply(3), invoice.total());
    }

    @Test
    void reservationInThePastIsRejected() {
        LocalDate today = LocalDate.now();

        assertThrows(InvalidBookingException.class,
                () -> bookingService.bookRoom(31, singleGuest(), today.minusDays(2), today.minusDays(1)));
    }

    @Test
    void overlappingReservationForTheSameRoomIsRejected() {
        LocalDate today = LocalDate.now();
        bookingService.bookRoom(31, singleGuest(), today, today.plusDays(3));

        assertThrows(RoomNotAvailableException.class,
                () -> bookingService.bookRoom(31, singleGuest(), today, today.plusDays(1)));
        assertThrows(RoomNotAvailableException.class,
                () -> bookingService.bookRoom(31, singleGuest(), today.plusDays(2), today.plusDays(4)));
        // Adjacent stays do not overlap, so this is allowed.
        bookingService.bookRoom(31, singleGuest(), today.plusDays(3), today.plusDays(5));
    }

    @Test
    void nonOverlappingFutureReservationsForTheSameRoomAreAllowed() {
        LocalDate today = LocalDate.now();

        Booking first = bookingService.bookRoom(31, singleGuest(), today.plusDays(2), today.plusDays(4));
        Booking second = bookingService.bookRoom(31, singleGuest(), today.plusDays(5), today.plusDays(6));

        assertTrue(first.isReserved());
        assertTrue(second.isReserved());
        assertEquals(2, bookingService.openBookings().size());
    }

    @Test
    void futureReservationIsNotYetCheckoutAble() {
        LocalDate today = LocalDate.now();
        bookingService.bookRoom(31, singleGuest(), today.plusDays(1), today.plusDays(2));

        assertThrows(BookingNotFoundException.class, () -> bookingService.checkout(31));
        assertThrows(BookingNotFoundException.class, () -> bookingService.getInHouseBooking(31));
    }

    @Test
    void availabilityQueriesAreRangeAware() {
        LocalDate today = LocalDate.now();
        bookingService.bookRoom(31, singleGuest(), today, today.plusDays(2));

        assertFalse(bookingService.isAvailable(31, today.plusDays(1), today.plusDays(2)));
        assertTrue(bookingService.isAvailable(31, today.plusDays(2), today.plusDays(3)));
    }

    // --- booking history ---

    @Test
    void checkoutRecordsTheInvoiceInHistory() {
        bookingService.bookRoom(31, singleGuest());
        bookingService.bookRoom(32, singleGuest());
        bookingService.checkout(31);

        assertEquals(1, bookingService.bookingHistory().size());
        Invoice invoice = bookingService.bookingHistory().get(0);
        assertEquals(31, invoice.roomNumber());
        assertEquals(1, invoice.nights());
        assertEquals(Money.rupees(2200), invoice.total());
    }
}