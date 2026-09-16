package com.abhiram.hotel.domain;

import com.abhiram.hotel.exception.InvalidBookingException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * A guest's stay in a particular {@link Room}, from check-in to check-out. A double room requires
 * exactly two guests; a single room requires exactly one, enforced at construction so an invalid
 * booking can never be created.
 *
 * <p>A booking spans one or more nights ({@code checkInDate} inclusive, {@code checkOutDate}
 * exclusive) and is {@link BookingStatus#RESERVED} before its check-in date,
 * {@link BookingStatus#ACTIVE} while the guest is in house, and {@link BookingStatus#CHECKED_OUT}
 * after the stay ends. The status is derived from the dates so that a booking restored from
 * persistence always reflects its current lifecycle position.
 */
public final class Booking {

    private final String bookingId;
    private final int roomNumber;
    private final RoomType roomType;
    private final List<Guest> guests;
    private final LocalDate checkInDate;
    private final LocalDate checkOutDate;
    private final FoodOrder foodOrder = new FoodOrder();
    private BookingStatus status;

    public Booking(int roomNumber, RoomType roomType, List<Guest> guests,
                   LocalDate checkInDate, LocalDate checkOutDate) {
        if (guests == null || guests.size() != roomType.occupancy()) {
            throw new InvalidBookingException(
                    "Room type " + roomType + " requires exactly " + roomType.occupancy()
                            + " guest(s), but " + (guests == null ? 0 : guests.size()) + " were provided");
        }
        if (checkInDate == null || checkOutDate == null) {
            throw new InvalidBookingException("Check-in and check-out dates must not be null");
        }
        if (!checkOutDate.isAfter(checkInDate)) {
            throw new InvalidBookingException(
                    "Check-out date " + checkOutDate + " must be after check-in date " + checkInDate);
        }
        this.bookingId = UUID.randomUUID().toString();
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.guests = List.copyOf(guests);
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.status = deriveStatus(checkInDate, checkOutDate, LocalDate.now());
    }

    private static BookingStatus deriveStatus(LocalDate checkIn, LocalDate checkOut, LocalDate today) {
        if (!today.isBefore(checkIn) && today.isBefore(checkOut)) {
            return BookingStatus.ACTIVE;
        }
        if (checkIn.isAfter(today)) {
            return BookingStatus.RESERVED;
        }
        return BookingStatus.CHECKED_OUT;
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

    public LocalDate checkInDate() {
        return checkInDate;
    }

    public LocalDate checkOutDate() {
        return checkOutDate;
    }

    /** Number of charged nights: the days between check-in (inclusive) and check-out (exclusive). */
    public long nights() {
        return ChronoUnit.DAYS.between(checkInDate, checkOutDate);
    }

    /** Whether the booking's stay covers {@code date} (check-in night inclusive, check-out day
     * exclusive). */
    public boolean covers(LocalDate date) {
        return !date.isBefore(checkInDate) && date.isBefore(checkOutDate);
    }

    /** Whether the guest is currently in house (check-in reached, check-out not yet). */
    public boolean isInHouse() {
        return status == BookingStatus.ACTIVE;
    }

    /** Whether this is an advance reservation whose check-in date is still in the future. */
    public boolean isReserved() {
        return status == BookingStatus.RESERVED;
    }

    /** Whether the stay has run past its check-out date without being checked out. */
    public boolean isPastStay() {
        return !LocalDate.now().isBefore(checkOutDate);
    }

    /** Whether the booking is still open (reserved or in house, i.e. not yet checked out). */
    public boolean isOpen() {
        return status == BookingStatus.RESERVED || status == BookingStatus.ACTIVE;
    }

    /** Whether the booking is still open. Synonym of {@link #isOpen()}, kept because callers
     * naturally think of a booking as "active" until it is checked out. */
    public boolean isActive() {
        return isOpen();
    }

    public BookingStatus status() {
        return status;
    }

    public FoodOrder foodOrder() {
        return foodOrder;
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