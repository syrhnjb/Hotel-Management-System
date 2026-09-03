package com.abhiram.hotel.domain;

import com.abhiram.hotel.exception.ValidationException;
import java.util.Objects;

/** Immutable record of a single guest's identifying details for a booking. */
public final class Guest {

    private final String name;
    private final ContactNumber contactNumber;
    private final String gender;

    public Guest(String name, ContactNumber contactNumber, String gender) {
        if (name == null || name.isBlank()) {
            throw new ValidationException("Guest name must not be empty");
        }
        if (gender == null || gender.isBlank()) {
            throw new ValidationException("Guest gender must not be empty");
        }
        this.name = name.trim();
        this.contactNumber = Objects.requireNonNull(contactNumber, "contactNumber must not be null");
        this.gender = gender.trim();
    }

    public String name() {
        return name;
    }

    public ContactNumber contactNumber() {
        return contactNumber;
    }

    public String gender() {
        return gender;
    }

    @Override
    public String toString() {
        return name;
    }
}
