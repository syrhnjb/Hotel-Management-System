package com.abhiram.hotel.domain;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * An immutable, fully-computed bill for a checked-out booking. Deliberately holds no behaviour
 * beyond simple accessors, so it can be rendered by the CLI or serialized without depending on
 * mutable domain state.
 */
public final class Invoice {

    private final int roomNumber;
    private final RoomType roomType;
    private final LocalDate checkInDate;
    private final LocalDate checkOutDate;
    private final long nights;
    private final String guestName;
    private final Money roomCharge;
    private final List<FoodOrderLine> foodLines;
    private final Money foodCharge;
    private final Money total;

    public Invoice(int roomNumber, RoomType roomType, LocalDate checkInDate, LocalDate checkOutDate,
                   String guestName, Money roomCharge, List<FoodOrderLine> foodLines, Money foodCharge) {
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.nights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        this.guestName = guestName;
        this.roomCharge = roomCharge;
        this.foodLines = List.copyOf(foodLines);
        this.foodCharge = foodCharge;
        this.total = roomCharge.add(foodCharge);
    }

    public int roomNumber() {
        return roomNumber;
    }

    public RoomType roomType() {
        return roomType;
    }

    public LocalDate checkInDate() {
        return checkInDate;
    }

    public LocalDate checkOutDate() {
        return checkOutDate;
    }

    public long nights() {
        return nights;
    }

    public String guestName() {
        return guestName;
    }

    public Money roomCharge() {
        return roomCharge;
    }

    public List<FoodOrderLine> foodLines() {
        return foodLines;
    }

    public Money foodCharge() {
        return foodCharge;
    }

    public Money total() {
        return total;
    }
}