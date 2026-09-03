package com.abhiram.hotel.domain;

import com.abhiram.hotel.exception.InvalidBookingException;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * A guest's stay in a particular {@link Room}, from check-in through checkout. A double room
 * requires exactly two guests; a single room requires exactly one, enforced at construction so
 * an invalid booking can never be created.
 */
public final class Booking {

    private final String bookingId;
    private final int roomNumber;
    private final RoomType roomType;
    private final List<Guest> guests;
    private final Instant checkInTime;
    private final FoodOrder foodOrder = new FoodOrder();
    private BookingStatus status = BookingStatus.ACTIVE;

    public Booking(int roomNumber, RoomType roomType, List<Guest> guests) {
        if (guests == null || guests.size() != roomType.occupancy()) {
            throw new InvalidBookingException(
                    "Room type " + roomType + " requires exactly " + roomType.occupancy()
                            + " guest(s), but " + (guests == null ? 0 : guests.size()) + " were provided");
        }
        this.bookingId = UUID.randomUUID().toString();
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.guests = List.copyOf(guests);
        this.checkInTime = Instant.now();
    }

    public String bookingId() {
        return bookingId;
    }

    public int roomNumber() {
        return roomNumber;
    }

    public RoomType roomType() {
        return roomType;
    }

    public List<Guest> guests() {
        return guests;
    }

    public Guest primaryGuest() {
        return guests.get(0);
    }

    public Instant checkInTime() {
        return checkInTime;
    }

    public FoodOrder foodOrder() {
        return foodOrder;
    }

    public BookingStatus status() {
        return status;
    }

    public boolean isActive() {
        return status == BookingStatus.ACTIVE;
    }

    public void markCheckedOut() {
        if (status == BookingStatus.CHECKED_OUT) {
            throw new InvalidBookingException("Booking " + bookingId + " is already checked out");
        }
        this.status = BookingStatus.CHECKED_OUT;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Booking)) return false;
        return bookingId.equals(((Booking) o).bookingId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookingId);
    }
}
