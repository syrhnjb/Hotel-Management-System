package com.abhiram.hotel.service;

import com.abhiram.hotel.domain.Booking;
import com.abhiram.hotel.domain.Guest;
import com.abhiram.hotel.domain.Invoice;
import com.abhiram.hotel.domain.Room;
import com.abhiram.hotel.domain.RoomType;
import com.abhiram.hotel.exception.BookingNotFoundException;
import com.abhiram.hotel.exception.RoomNotAvailableException;
import com.abhiram.hotel.repository.BookingRepository;
import com.abhiram.hotel.repository.RoomRepository;
import java.util.List;

/**
 * Orchestrates the booking lifecycle: creating a booking (which atomically marks the room
 * occupied) and checking out (which computes the final invoice and releases the room).
 *
 * <p>Both operations are written so that a failure partway through cannot leave room status and
 * booking records inconsistent with each other--the room is only mutated after every validation
 * has already passed, and the room is only released after the booking has been marked checked
 * out and its invoice has been computed.
 */
public final class BookingService {

    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final BillingService billingService;

    public BookingService(RoomRepository roomRepository, BookingRepository bookingRepository,
                           BillingService billingService) {
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
        this.billingService = billingService;
    }

    /** Books {@code roomNumber} for the given guests. Throws if the room does not exist, is
     * already occupied, or the guest count does not match the room type's occupancy. */
    public Booking bookRoom(int roomNumber, List<Guest> guests) {
        Room room = roomRepository.findByRoomNumber(roomNumber)
                .orElseThrow(() -> new com.abhiram.hotel.exception.RoomNotFoundException(roomNumber));
        if (!room.isAvailable()) {
            throw new RoomNotAvailableException(roomNumber);
        }

        // Validate before mutating room state, so a rejected booking never partially occupies a room.
        Booking booking = new Booking(roomNumber, room.roomType(), guests);

        room.occupy();
        bookingRepository.save(booking);
        return booking;
    }

    /** Restores a booking that was reloaded from persisted state (used only at startup). */
    public void restoreBooking(Booking booking) {
        Room room = roomRepository.findByRoomNumber(booking.roomNumber())
                .orElseThrow(() -> new com.abhiram.hotel.exception.RoomNotFoundException(booking.roomNumber()));
        room.occupy();
        bookingRepository.save(booking);
    }

    public Booking getActiveBooking(int roomNumber) {
        return bookingRepository.findActiveByRoomNumber(roomNumber)
                .orElseThrow(() -> new BookingNotFoundException(roomNumber));
    }

    /** Checks out {@code roomNumber}: computes the invoice, marks the booking checked out, and
     * releases the room for future bookings. */
    public Invoice checkout(int roomNumber) {
        Booking booking = getActiveBooking(roomNumber);
        Invoice invoice = billingService.generateInvoice(booking);

        booking.markCheckedOut();
        bookingRepository.remove(roomNumber);
        roomRepository.findByRoomNumber(roomNumber).ifPresent(Room::release);

        return invoice;
    }

    public List<Booking> allActiveBookings() {
        return bookingRepository.findAllActive();
    }

    /** Exposed for room-type lookups used by the CLI menu (e.g. "book by type" flow). */
    public RoomType roomTypeOf(int roomNumber) {
        return roomRepository.findByRoomNumber(roomNumber)
                .orElseThrow(() -> new com.abhiram.hotel.exception.RoomNotFoundException(roomNumber))
                .roomType();
    }
}
