package com.abhiram.hotel.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.abhiram.hotel.domain.Booking;
import com.abhiram.hotel.domain.ContactNumber;
import com.abhiram.hotel.domain.FoodOrderLine;
import com.abhiram.hotel.domain.Guest;
import com.abhiram.hotel.domain.MenuItem;
import com.abhiram.hotel.domain.RoomType;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileHotelStateRepositoryTest {

    @TempDir
    Path tempDir;

    @Test
    void loadingFromAMissingFileReturnsAnEmptyList() {
        FileHotelStateRepository repo = new FileHotelStateRepository(tempDir.resolve("does-not-exist.txt"));

        assertTrue(repo.loadActiveBookings().isEmpty());
    }

    @Test
    void savedBookingsRoundTripCorrectly() {
        FileHotelStateRepository repo = new FileHotelStateRepository(tempDir.resolve("state.txt"));

        Booking singleBooking = new Booking(31, RoomType.LUXURY_SINGLE,
                List.of(new Guest("Asha", ContactNumber.of("9876543210"), "F")));
        singleBooking.foodOrder().addLine(new FoodOrderLine(MenuItem.PASTA, 2));
        singleBooking.foodOrder().addLine(new FoodOrderLine(MenuItem.COKE, 1));

        Booking doubleBooking = new Booking(1, RoomType.LUXURY_DOUBLE,
                List.of(new Guest("Ravi", ContactNumber.of("9876500000"), "M"),
                        new Guest("Meera", ContactNumber.of("9876511111"), "F")));

        repo.saveActiveBookings(List.of(singleBooking, doubleBooking));

        List<Booking> reloaded = repo.loadActiveBookings();

        assertEquals(2, reloaded.size());

        Booking reloadedSingle = reloaded.stream().filter(b -> b.roomNumber() == 31).findFirst().orElseThrow();
        assertEquals("Asha", reloadedSingle.primaryGuest().name());
        assertEquals(RoomType.LUXURY_SINGLE, reloadedSingle.roomType());
        assertEquals(2, reloadedSingle.foodOrder().lines().size());

        Booking reloadedDouble = reloaded.stream().filter(b -> b.roomNumber() == 1).findFirst().orElseThrow();
        assertEquals(2, reloadedDouble.guests().size());
        assertEquals("Ravi", reloadedDouble.guests().get(0).name());
        assertEquals("Meera", reloadedDouble.guests().get(1).name());
    }

    @Test
    void bookingWithNoFoodOrdersRoundTrips() {
        FileHotelStateRepository repo = new FileHotelStateRepository(tempDir.resolve("state.txt"));
        Booking booking = new Booking(41, RoomType.DELUXE_SINGLE,
                List.of(new Guest("Kiran", ContactNumber.of("9998887777"), "M")));

        repo.saveActiveBookings(List.of(booking));
        List<Booking> reloaded = repo.loadActiveBookings();

        assertEquals(1, reloaded.size());
        assertTrue(reloaded.get(0).foodOrder().lines().isEmpty());
    }
}
