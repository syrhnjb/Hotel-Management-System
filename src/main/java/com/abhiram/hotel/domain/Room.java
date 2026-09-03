package com.abhiram.hotel.domain;

import com.abhiram.hotel.exception.RoomNotAvailableException;

/**
 * A physical hotel room. Mutable only with respect to {@link RoomStatus}; identity
 * ({@code roomNumber}) and {@code roomType} never change after construction.
 */
public final class Room {

    private final int roomNumber;
    private final RoomType roomType;
    private RoomStatus status;

    public Room(int roomNumber, RoomType roomType) {
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.status = RoomStatus.AVAILABLE;
    }

    public int roomNumber() {
        return roomNumber;
    }

    public RoomType roomType() {
        return roomType;
    }

    public RoomStatus status() {
        return status;
    }

    public boolean isAvailable() {
        return status == RoomStatus.AVAILABLE;
    }

    /** Marks the room occupied. Throws if it is already occupied. */
    public void occupy() {
        if (status == RoomStatus.OCCUPIED) {
            throw new RoomNotAvailableException(roomNumber);
        }
        this.status = RoomStatus.OCCUPIED;
    }

    /** Marks the room available again (called on checkout). Idempotent-safe no-op guard is
     * intentionally not applied here; callers must check state via {@link #isAvailable()}
     * beforehand so double-checkout is caught explicitly by the service layer. */
    public void release() {
        this.status = RoomStatus.AVAILABLE;
    }

    @Override
    public String toString() {
        return "Room " + roomNumber + " (" + roomType + ", " + status + ")";
    }
}
