package com.abhiram.hotel.domain;

/** Lifecycle of a {@link Booking}: ACTIVE while the guest is checked in, CHECKED_OUT after. */
public enum BookingStatus {
    ACTIVE,
    CHECKED_OUT
}
