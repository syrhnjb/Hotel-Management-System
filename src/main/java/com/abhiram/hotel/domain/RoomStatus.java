package com.abhiram.hotel.domain;

/** Lifecycle states of a {@link Room}. Kept intentionally simple: AVAILABLE &lt;-&gt; OCCUPIED. */
public enum RoomStatus {
    AVAILABLE,
    OCCUPIED
}
