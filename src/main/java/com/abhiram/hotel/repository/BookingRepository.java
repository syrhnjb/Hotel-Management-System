package com.abhiram.hotel.repository;

import com.abhiram.hotel.domain.Booking;
import java.util.List;
import java.util.Optional;

/** Tracks active (not-yet-checked-out) bookings, keyed by room number. */
public interface BookingRepository {

    void save(Booking booking);

    Optional<Booking> findActiveByRoomNumber(int roomNumber);

    void remove(int roomNumber);

    List<Booking> findAllActive();
}
