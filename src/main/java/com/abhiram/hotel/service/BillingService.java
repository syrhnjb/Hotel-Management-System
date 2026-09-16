package com.abhiram.hotel.service;

import com.abhiram.hotel.domain.Booking;
import com.abhiram.hotel.domain.Invoice;
import com.abhiram.hotel.domain.Money;

/**
 * Pure billing calculations, deliberately free of any console I/O or persistence so it can be
 * unit tested as plain arithmetic against a {@link Booking}.
 *
 * <p>Room charges are prorated by the number of nights in the booking: {@code dailyRate * nights}.
 */
public final class BillingService {

    public Invoice generateInvoice(Booking booking) {
        long nights = booking.nights();
        Money roomCharge = booking.roomType().dailyRate().multiply((int) nights);
        Money foodCharge = booking.foodOrder().total();
        return new Invoice(booking.roomNumber(), booking.roomType(),
                booking.checkInDate(), booking.checkOutDate(),
                booking.primaryGuest().name(),
                roomCharge, booking.foodOrder().lines(), foodCharge);
    }
}