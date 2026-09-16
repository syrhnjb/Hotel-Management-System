package com.abhiram.hotel.repository;

import com.abhiram.hotel.domain.Booking;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** In-memory {@link BookingRepository}. Not thread-safe; the CLI is single-threaded. */
public final class InMemoryBookingRepository implements BookingRepository {

    private final Map<String, Booking> bookingsById = new LinkedHashMap<>();

    @Override
    public void save(Booking booking) {
        bookingsById.put(booking.bookingId(), booking);
    }

    @Override
    public Optional<Booking> findInHouseByRoomNumber(int roomNumber) {
        LocalDate today = LocalDate.now();
        return bookingsById.values().stream()
                .filter(b -> b.roomNumber() == roomNumber)
                .filter(Booking::isInHouse)
                .filter(b -> b.covers(today))
                .findFirst();
    }

    @Override
    public boolean hasOverlap(int roomNumber, LocalDate checkInDate, LocalDate checkOutDate) {
        return bookingsById.values().stream()
                .filter(b -> b.roomNumber() == roomNumber)
                .filter(Booking::isOpen)
                .anyMatch(b -> rangesOverlap(b, checkInDate, checkOutDate));
    }

    /** Two half-open ranges {@code [aIn, aOut)} and {@code [bIn, bOut)} overlap iff
     * {@code aIn < bOut && bIn < aOut}. */
    private boolean rangesOverlap(Booking booking, LocalDate checkInDate, LocalDate checkOutDate) {
        return checkInDate.isBefore(booking.checkOutDate()) && booking.checkInDate().isBefore(checkOutDate);
    }

    @Override
    public List<Booking> findAllOpen() {
        return bookingsById.values().stream().filter(Booking::isOpen).toList();
    }

    @Override
    public void remove(String bookingId) {
        bookingsById.remove(bookingId);
    }
}