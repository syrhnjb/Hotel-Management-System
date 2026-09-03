package com.abhiram.hotel.exception;

/**
 * Base type for all domain-rule violations in the application (invalid bookings, unavailable
 * rooms, bad input, and so on).
 *
 * <p>These are unchecked ({@code RuntimeException}) by design: every one of them represents a
 * business-rule violation that the CLI layer catches at a single, well-defined boundary and
 * reports to the user. Making them checked would force every service method up the call chain to
 * either declare or wrap them, without adding real safety--the CLI already has to handle all of
 * them uniformly. See {@code docs/design-decisions.md} for the full rationale.
 */
public abstract class HotelDomainException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    protected HotelDomainException(String message) {
        super(message);
    }

    protected HotelDomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
