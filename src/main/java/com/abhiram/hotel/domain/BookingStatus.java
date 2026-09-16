package com.abhiram.hotel.domain;

/** Lifecycle of a {@link Booking}: RESERVED before check-in, ACTIVE while the guest is in house,
 * CHECKED_OUT after the stay ends. The status is derived from the booking's check-in and
 * check-out dates relative to today, and only ever transitions to CHECKED_OUT. */
public enum BookingStatus {
    RESERVED,
    ACTIVE,
    CHECKED_OUT
}