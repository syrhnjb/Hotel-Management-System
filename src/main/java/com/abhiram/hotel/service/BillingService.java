package com.abhiram.hotel.service;

import com.abhiram.hotel.domain.Booking;
import com.abhiram.hotel.domain.Invoice;
import com.abhiram.hotel.domain.Money;

/**
 * Pure billing calculations, deliberately free of any console I/O or persistence so it can be
 * unit tested as plain arithmetic against a {@link Booking}.
 */
public final class BillingService {

    public Invoice generateInvoice(Booking booking) {
        Money roomCharge = booking.roomType().dailyRate();
        Money foodCharge = booking.foodOrder().total();
        return new Invoice(booking.roomNumber(), booking.roomType(), roomCharge,
                booking.foodOrder().lines(), foodCharge);
    }
}
