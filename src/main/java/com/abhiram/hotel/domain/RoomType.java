package com.abhiram.hotel.domain;

/**
 * The four room categories the hotel offers, mirroring the original application's catalogue but
 * replacing magic numbers and switch statements with a single source of truth for each type's
 * capacity, price, and amenities.
 */
public enum RoomType {

    LUXURY_DOUBLE(1, 10, 2, Money.rupees(4000), true, true),
    DELUXE_DOUBLE(11, 30, 2, Money.rupees(3000), false, true),
    LUXURY_SINGLE(31, 40, 1, Money.rupees(2200), true, true),
    DELUXE_SINGLE(41, 60, 1, Money.rupees(1200), false, true);

    private final int firstRoomNumber;
    private final int lastRoomNumber;
    private final int occupancy;
    private final Money dailyRate;
    private final boolean airConditioned;
    private final boolean breakfastIncluded;

    RoomType(int firstRoomNumber, int lastRoomNumber, int occupancy, Money dailyRate,
             boolean airConditioned, boolean breakfastIncluded) {
        this.firstRoomNumber = firstRoomNumber;
        this.lastRoomNumber = lastRoomNumber;
        this.occupancy = occupancy;
        this.dailyRate = dailyRate;
        this.airConditioned = airConditioned;
        this.breakfastIncluded = breakfastIncluded;
    }

    public int firstRoomNumber() {
        return firstRoomNumber;
    }

    public int lastRoomNumber() {
        return lastRoomNumber;
    }

    /** Number of guests this room type is designed to hold (1 = single, 2 = double). */
    public int occupancy() {
        return occupancy;
    }

    public Money dailyRate() {
        return dailyRate;
    }

    public boolean isAirConditioned() {
        return airConditioned;
    }

    public boolean isBreakfastIncluded() {
        return breakfastIncluded;
    }

    /** Resolves which {@link RoomType} a given hotel-wide room number belongs to. */
    public static RoomType forRoomNumber(int roomNumber) {
        for (RoomType type : values()) {
            if (roomNumber >= type.firstRoomNumber && roomNumber <= type.lastRoomNumber) {
                return type;
            }
        }
        throw new IllegalArgumentException("No room type covers room number " + roomNumber);
    }
}
