package com.abhiram.hotel.service;

import com.abhiram.hotel.domain.Booking;
import com.abhiram.hotel.domain.Guest;
import com.abhiram.hotel.domain.Invoice;
import com.abhiram.hotel.domain.Room;
import com.abhiram.hotel.domain.RoomType;
import com.abhiram.hotel.exception.BookingNotFoundException;
import com.abhiram.hotel.exception.InvalidBookingException;
import com.abhiram.hotel.exception.RoomNotAvailableException;
import com.abhiram.hotel.exception.RoomNotFoundException;
import com.abhiram.hotel.repository.BookingRepository;
import com.abhiram.hotel.repository.InvoiceHistoryRepository;
import com.abhiram.hotel.repository.RoomRepository;
import java.time.LocalDate;
import java.util.List;

/**
 * Orchestrates the booking lifecycle: creating a reservation (which atomically marks the room
 * occupied if the guest checks in today) and checking out (which computes the final invoice,
 * records it in the invoice history, and releases the room).
 *
 * <p>Both operations are written so that a failure partway through cannot leave room status and
 * booking records inconsistent with each other--the room is only mutated after every validation
 * has already passed, and the room is only released after the booking has been marked checked
 * out and its invoice has been recorded.
 */
public final class BookingService {

    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final BillingService billingService;
    private final InvoiceHistoryRepository historyRepository;

    public BookingService(RoomRepository roomRepository, BookingRepository bookingRepository,
                          BillingService billingService, InvoiceHistoryRepository historyRepository) {
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
        this.billingService = billingService;
        this.historyRepository = historyRepository;
    }

    /** Books {@code roomNumber} for the given guests for tonight (check-in today, check-out
     * tomorrow). Convenience overload over {@link #bookRoom(int, List, LocalDate, LocalDate)}. */
    public Booking bookRoom(int roomNumber, List<Guest> guests) {
        LocalDate today = LocalDate.now();
        return bookRoom(roomNumber, guests, today, today.plusDays(1));
    }

    /** Books {@code roomNumber} for the given guests over {@code [checkInDate, checkOutDate)}.
     * Throws if the room does not exist, the range is in the past, an open booking already
     * overlaps the range, or the guest count does not match the room type's occupancy. */
    public Booking bookRoom(int roomNumber, List<Guest> guests,
                            LocalDate checkInDate, LocalDate checkOutDate) {
        Room room = roomRepository.findByRoomNumber(roomNumber)
                .orElseThrow(() -> new RoomNotFoundException(roomNumber));
        if (checkInDate.isBefore(LocalDate.now())) {
            throw new InvalidBookingException("Check-in date " + checkInDate + " is in the past");
        }
        if (bookingRepository.hasOverlap(roomNumber, checkInDate, checkOutDate)) {
            throw new RoomNotAvailableException(roomNumber);
        }

        // Validate before mutating room state, so a rejected booking never partially occupies a room.
        Booking booking = new Booking(roomNumber, room.roomType(), guests, checkInDate, checkOutDate);

        if (booking.isInHouse()) {
            room.occupy();
        }
        bookingRepository.save(booking);
        return booking;
    }

    /** Restores a booking that was reloaded from persisted state (used only at startup). */
    public void restoreBooking(Booking booking) {
        Room room = roomRepository.findByRoomNumber(booking.roomNumber())
                .orElseThrow(() -> new RoomNotFoundException(booking.roomNumber()));
        if (booking.isInHouse()) {
            room.occupy();
        }
        bookingRepository.save(booking);
    }

    /** The booking that is currently occupying {@code roomNumber} (the in-house stay covering
     * today). Throws if the room is empty or its guest is not yet checked in. */
    public Booking getInHouseBooking(int roomNumber) {
        return bookingRepository.findInHouseByRoomNumber(roomNumber)
                .orElseThrow(() -> new BookingNotFoundException(roomNumber));
    }

    /** Checks out the in-house stay for {@code roomNumber}: computes the invoice, marks the
     * booking checked out, persists the invoice to history, and releases the room. */
    public Invoice checkout(int roomNumber) {
        Booking booking = getInHouseBooking(roomNumber);
        Invoice invoice = billingService.generateInvoice(booking);

        booking.markCheckedOut();
        bookingRepository.remove(booking.bookingId());
        roomRepository.findByRoomNumber(roomNumber).ifPresent(Room::release);
        historyRepository.save(invoice);

        return invoice;
    }

    public List<Booking> openBookings() {
        return bookingRepository.findAllOpen();
    }

    /** All invoices recorded for previously checked-out bookings. */
    public List<Invoice> bookingHistory() {
        return historyRepository.loadAll();
    }

    /** Whether {@code roomNumber} has no open booking overlapping {@code [checkInDate, checkOutDate)}. */
    public boolean isAvailable(int roomNumber, LocalDate checkInDate, LocalDate checkOutDate) {
        return !bookingRepository.hasOverlap(roomNumber, checkInDate, checkOutDate);
    }

    public List<Room> availableRoomsOfType(RoomType type, LocalDate checkInDate, LocalDate checkOutDate) {
        return roomRepository.findByType(type).stream()
                .filter(room -> isAvailable(room.roomNumber(), checkInDate, checkOutDate))
                .toList();
    }

    public int countAvailable(RoomType type, LocalDate checkInDate, LocalDate checkOutDate) {
        return availableRoomsOfType(type, checkInDate, checkOutDate).size();
    }

    /** Exposed for room-type lookups used by the CLI menu (e.g. "book by type" flow). */
    public RoomType roomTypeOf(int roomNumber) {
        return roomRepository.findByRoomNumber(roomNumber)
                .orElseThrow(() -> new com.abhiram.hotel.exception.RoomNotFoundException(roomNumber))
                .roomType();
    }
}