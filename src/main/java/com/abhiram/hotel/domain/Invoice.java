package com.abhiram.hotel.domain;

import java.util.List;

/**
 * An immutable, fully-computed bill for a checked-out booking. Deliberately holds no behaviour
 * beyond simple accessors, so it can be rendered by the CLI or serialized without depending on
 * mutable domain state.
 */
public final class Invoice {

    private final int roomNumber;
    private final RoomType roomType;
    private final Money roomCharge;
    private final List<FoodOrderLine> foodLines;
    private final Money foodCharge;
    private final Money total;

    public Invoice(int roomNumber, RoomType roomType, Money roomCharge,
                    List<FoodOrderLine> foodLines, Money foodCharge) {
        this.roomNumber = roomNumber;
        this.roomType = roomType;
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
