package com.abhiram.hotel.service;

import com.abhiram.hotel.domain.Booking;
import com.abhiram.hotel.domain.FoodOrderLine;
import com.abhiram.hotel.domain.MenuItem;

/** Adds food line items to a room's active booking. */
public final class FoodOrderService {

    private final BookingService bookingService;

    public FoodOrderService(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /** Adds a food line to the active booking for {@code roomNumber}. Throws
     * {@link com.abhiram.hotel.exception.BookingNotFoundException} if the room has no active
     * booking, and {@link com.abhiram.hotel.exception.InvalidFoodOrderException} for a bad
     * quantity. */
    public void addFoodOrder(int roomNumber, MenuItem menuItem, int quantity) {
        Booking booking = bookingService.getActiveBooking(roomNumber);
        booking.foodOrder().addLine(new FoodOrderLine(menuItem, quantity));
    }
}
