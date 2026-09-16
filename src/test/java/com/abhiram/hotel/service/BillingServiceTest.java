package com.abhiram.hotel.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.abhiram.hotel.domain.Booking;
import com.abhiram.hotel.domain.ContactNumber;
import com.abhiram.hotel.domain.FoodOrderLine;
import com.abhiram.hotel.domain.Guest;
import com.abhiram.hotel.domain.Invoice;
import com.abhiram.hotel.domain.MenuItem;
import com.abhiram.hotel.domain.Money;
import com.abhiram.hotel.domain.RoomType;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Billing is pure arithmetic over a {@link Booking}, so it is tested without any repository or
 * console involvement at all. */
class BillingServiceTest {

    private final BillingService billingService = new BillingService();

    private final LocalDate today = LocalDate.now();

    private Guest guest(String name) {
        return new Guest(name, ContactNumber.of("9876543210"), "F");
    }

    private Booking booking(int roomNumber, RoomType type, int nights, List<Guest> guests) {
        return new Booking(roomNumber, type, guests, today, today.plusDays(nights));
    }

    @Test
    void roomOnlyBillChargesJustTheDailyRate() {
        Booking booking = booking(31, RoomType.LUXURY_SINGLE, 1, List.of(guest("Asha")));

        Invoice invoice = billingService.generateInvoice(booking);

        assertEquals(Money.rupees(2200), invoice.roomCharge());
        assertEquals(Money.ZERO, invoice.foodCharge());
        assertEquals(Money.rupees(2200), invoice.total());
    }

    @Test
    void multiNightStayChargesTheDailyRatePerNight() {
        Booking booking = booking(31, RoomType.LUXURY_SINGLE, 3, List.of(guest("Asha")));

        Invoice invoice = billingService.generateInvoice(booking);

        assertEquals(3, invoice.nights());
        assertEquals(Money.rupees(6600), invoice.roomCharge());
        assertEquals(Money.rupees(6600), invoice.total());
    }

    @Test
    void roomPlusSingleFoodItemIsSummed() {
        Booking booking = booking(41, RoomType.DELUXE_SINGLE, 1, List.of(guest("Ravi")));
        booking.foodOrder().addLine(new FoodOrderLine(MenuItem.PASTA, 2));

        Invoice invoice = billingService.generateInvoice(booking);

        assertEquals(Money.rupees(1200), invoice.roomCharge());
        assertEquals(Money.rupees(120), invoice.foodCharge());
        assertEquals(Money.rupees(1320), invoice.total());
    }

    @Test
    void roomPlusMultipleFoodItemsAreSummed() {
        Booking booking = booking(1, RoomType.LUXURY_DOUBLE, 1,
                List.of(guest("Asha"), guest("Ravi")));
        booking.foodOrder().addLine(new FoodOrderLine(MenuItem.PASTA, 2));   // 120
        booking.foodOrder().addLine(new FoodOrderLine(MenuItem.COKE, 2));    // 60
        booking.foodOrder().addLine(new FoodOrderLine(MenuItem.SANDWICH, 1)); // 50

        Invoice invoice = billingService.generateInvoice(booking);

        assertEquals(Money.rupees(4000), invoice.roomCharge());
        assertEquals(Money.rupees(230), invoice.foodCharge());
        assertEquals(Money.rupees(4230), invoice.total());
    }

    @Test
    void zeroFoodOrdersMeansFoodChargeIsZero() {
        Booking booking = booking(11, RoomType.DELUXE_DOUBLE, 2,
                List.of(guest("Asha"), guest("Ravi")));

        Invoice invoice = billingService.generateInvoice(booking);

        assertEquals(Money.ZERO, invoice.foodCharge());
        assertEquals(invoice.roomCharge(), invoice.total());
    }

    @Test
    void singleUnitQuantityIsPricedAtExactlyTheUnitPrice() {
        Booking booking = booking(31, RoomType.LUXURY_SINGLE, 1, List.of(guest("Asha")));
        booking.foodOrder().addLine(new FoodOrderLine(MenuItem.COKE, 1));

        Invoice invoice = billingService.generateInvoice(booking);

        assertEquals(Money.rupees(30), invoice.foodCharge());
    }
}