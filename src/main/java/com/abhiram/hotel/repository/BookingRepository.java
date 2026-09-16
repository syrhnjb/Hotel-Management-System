package com.abhiram.hotel.repository;

import com.abhiram.hotel.domain.Booking;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Tracks open (reserved or in-house) bookings, keyed by booking id. Rooms can hold several
 * non-overlapping bookings over time (e.g. back-to-back reservations), so availability must be
 * queried by date range rather than by a single mutable occupied flag.
 */
public interface BookingRepository {

    void save(Booking booking);

    /** The booking currently occupying {@code roomNumber} (the in-house stay covering today). */
    Optional<Booking> findInHouseByRoomNumber(int roomNumber);

    /** Whether any open booking for {@code roomNumber} overlaps the half-open range
     * {@code [checkInDate, checkOutDate)}. */
    boolean hasOverlap(int roomNumber, LocalDate checkInDate, LocalDate checkOutDate);

    /** All bookings that are not yet checked out (reserved + in-house). */
    List<Booking> findAllOpen();

    void remove(String bookingId);
}