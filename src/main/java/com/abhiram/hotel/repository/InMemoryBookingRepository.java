package com.abhiram.hotel.repository;

import com.abhiram.hotel.domain.Booking;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** In-memory {@link BookingRepository}. Not thread-safe; the CLI is single-threaded. */
public final class InMemoryBookingRepository implements BookingRepository {

    private final Map<Integer, Booking> activeBookingsByRoom = new LinkedHashMap<>();

    @Override
    public void save(Booking booking) {
        activeBookingsByRoom.put(booking.roomNumber(), booking);
    }

    @Override
    public Optional<Booking> findActiveByRoomNumber(int roomNumber) {
        return Optional.ofNullable(activeBookingsByRoom.get(roomNumber));
    }

    @Override
    public void remove(int roomNumber) {
        activeBookingsByRoom.remove(roomNumber);
    }

    @Override
    public List<Booking> findAllActive() {
        return new ArrayList<>(activeBookingsByRoom.values());
    }
}
