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
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileHotelStateRepositoryTest {

    @TempDir
    Path tempDir;

    private final LocalDate today = LocalDate.now();

    private Booking booking(int roomNumber, RoomType type, int nights, List<Guest> guests) {
        return new Booking(roomNumber, type, guests, today, today.plusDays(nights));
    }

    @Test
    void loadingFromAMissingFileReturnsAnEmptyList() {
        FileHotelStateRepository repo = new FileHotelStateRepository(tempDir.resolve("does-not-exist.txt"));

        assertTrue(repo.loadOpenBookings().isEmpty());
    }

    @Test
    void savedBookingsRoundTripCorrectly() {
        FileHotelStateRepository repo = new FileHotelStateRepository(tempDir.resolve("state.txt"));

        Booking singleBooking = booking(31, RoomType.LUXURY_SINGLE, 2,
                List.of(new Guest("Asha", ContactNumber.of("9876543210"), "F")));
        singleBooking.foodOrder().addLine(new FoodOrderLine(MenuItem.PASTA, 2));
        singleBooking.foodOrder().addLine(new FoodOrderLine(MenuItem.COKE, 1));

        Booking doubleBooking = booking(1, RoomType.LUXURY_DOUBLE, 1,
                List.of(new Guest("Ravi", ContactNumber.of("9876500000"), "M"),
                        new Guest("Meera", ContactNumber.of("9876511111"), "F")));

        repo.saveOpenBookings(List.of(singleBooking, doubleBooking));

        List<Booking> reloaded = repo.loadOpenBookings();

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
    void checkInAndCheckOutDatesSurviveTheRoundTrip() {
        FileHotelStateRepository repo = new FileHotelStateRepository(tempDir.resolve("state.txt"));
        Booking booking = booking(31, RoomType.LUXURY_SINGLE, 3,
                List.of(new Guest("Asha", ContactNumber.of("9876543210"), "F")));

        repo.saveOpenBookings(List.of(booking));
        List<Booking> reloaded = repo.loadOpenBookings();

        assertEquals(1, reloaded.size());
        assertEquals(today, reloaded.get(0).checkInDate());
        assertEquals(today.plusDays(3), reloaded.get(0).checkOutDate());
        assertEquals(3, reloaded.get(0).nights());
    }

    @Test
    void futureReservationRoundTripsAsReserved() {
        FileHotelStateRepository repo = new FileHotelStateRepository(tempDir.resolve("state.txt"));
        Booking booking = new Booking(41, RoomType.DELUXE_SINGLE,
                List.of(new Guest("Kiran", ContactNumber.of("9998887777"), "M")),
                today.plusDays(2), today.plusDays(4));

        repo.saveOpenBookings(List.of(booking));
        List<Booking> reloaded = repo.loadOpenBookings();

        assertEquals(1, reloaded.size());
        assertTrue(reloaded.get(0).isReserved());
    }

    @Test
    void bookingWithNoFoodOrdersRoundTrips() {
        FileHotelStateRepository repo = new FileHotelStateRepository(tempDir.resolve("state.txt"));
        Booking stay = booking(41, RoomType.DELUXE_SINGLE, 1,
                List.of(new Guest("Kiran", ContactNumber.of("9998887777"), "M")));

        repo.saveOpenBookings(List.of(stay));
        List<Booking> reloaded = repo.loadOpenBookings();

        assertEquals(1, reloaded.size());
        assertTrue(reloaded.get(0).foodOrder().lines().isEmpty());
    }
}