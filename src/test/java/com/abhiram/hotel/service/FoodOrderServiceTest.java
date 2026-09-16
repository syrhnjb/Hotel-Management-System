package com.abhiram.hotel.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.abhiram.hotel.domain.Booking;
import com.abhiram.hotel.domain.ContactNumber;
import com.abhiram.hotel.domain.Guest;
import com.abhiram.hotel.domain.MenuItem;
import com.abhiram.hotel.domain.Money;
import com.abhiram.hotel.exception.BookingNotFoundException;
import com.abhiram.hotel.exception.InvalidFoodOrderException;
import com.abhiram.hotel.repository.BookingRepository;
import com.abhiram.hotel.repository.InMemoryBookingRepository;
import com.abhiram.hotel.repository.InMemoryInvoiceHistoryRepository;
import com.abhiram.hotel.repository.InMemoryRoomRepository;
import com.abhiram.hotel.repository.RoomRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FoodOrderServiceTest {

    private BookingService bookingService;
    private FoodOrderService foodOrderService;

    @BeforeEach
    void setUp() {
        RoomRepository roomRepository = new InMemoryRoomRepository();
        BookingRepository bookingRepository = new InMemoryBookingRepository();
        bookingService = new BookingService(roomRepository, bookingRepository, new BillingService(),
                new InMemoryInvoiceHistoryRepository());
        foodOrderService = new FoodOrderService(bookingService);
    }

    private List<Guest> singleGuest() {
        return List.of(new Guest("Asha", ContactNumber.of("9876543210"), "F"));
    }

    @Test
    void addsAFoodLineToTheActiveBooking() {
        bookingService.bookRoom(31, singleGuest());

        foodOrderService.addFoodOrder(31, MenuItem.PASTA, 2);

        Booking booking = bookingService.getInHouseBooking(31);
        assertEquals(1, booking.foodOrder().lines().size());
        assertEquals(Money.rupees(120), booking.foodOrder().total());
    }

    @Test
    void orderingFoodForARoomWithNoBookingThrows() {
        assertThrows(BookingNotFoundException.class,
                () -> foodOrderService.addFoodOrder(31, MenuItem.PASTA, 1));
    }

    @Test
    void zeroQuantityIsRejected() {
        bookingService.bookRoom(31, singleGuest());

        assertThrows(InvalidFoodOrderException.class,
                () -> foodOrderService.addFoodOrder(31, MenuItem.PASTA, 0));
    }

    @Test
    void negativeQuantityIsRejected() {
        bookingService.bookRoom(31, singleGuest());

        assertThrows(InvalidFoodOrderException.class,
                () -> foodOrderService.addFoodOrder(31, MenuItem.PASTA, -3));
    }

    @Test
    void excessiveQuantityIsRejected() {
        bookingService.bookRoom(31, singleGuest());

        assertThrows(InvalidFoodOrderException.class,
                () -> foodOrderService.addFoodOrder(31, MenuItem.PASTA, 1_000));
    }

    @Test
    void multipleLinesAccumulateInTheOrder() {
        bookingService.bookRoom(31, singleGuest());

        foodOrderService.addFoodOrder(31, MenuItem.SANDWICH, 1);
        foodOrderService.addFoodOrder(31, MenuItem.COKE, 2);

        Booking booking = bookingService.getInHouseBooking(31);
        assertEquals(2, booking.foodOrder().lines().size());
        assertEquals(Money.rupees(50).add(Money.rupees(60)), booking.foodOrder().total());
    }
}
