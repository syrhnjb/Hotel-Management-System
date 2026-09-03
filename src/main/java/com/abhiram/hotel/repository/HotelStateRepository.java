package com.abhiram.hotel.repository;

import com.abhiram.hotel.domain.Booking;
import java.util.List;

/**
 * Persists and restores the hotel's active bookings between application runs.
 *
 * <p>This is the one boundary in the application that touches disk I/O for durability, kept
 * separate from {@link BookingRepository} (the in-memory source of truth while the app runs) so
 * the persistence mechanism can be swapped--or dropped entirely for tests--without touching
 * business logic.
 */
public interface HotelStateRepository {

    void saveActiveBookings(List<Booking> activeBookings);

    List<Booking> loadActiveBookings();
}
