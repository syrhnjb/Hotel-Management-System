package com.abhiram.hotel.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class RoomTypeTest {

    @Test
    void resolvesTheCorrectTypeAtEachRangeBoundary() {
        assertEquals(RoomType.LUXURY_DOUBLE, RoomType.forRoomNumber(1));
        assertEquals(RoomType.LUXURY_DOUBLE, RoomType.forRoomNumber(10));
        assertEquals(RoomType.DELUXE_DOUBLE, RoomType.forRoomNumber(11));
        assertEquals(RoomType.DELUXE_DOUBLE, RoomType.forRoomNumber(30));
        assertEquals(RoomType.LUXURY_SINGLE, RoomType.forRoomNumber(31));
        assertEquals(RoomType.LUXURY_SINGLE, RoomType.forRoomNumber(40));
        assertEquals(RoomType.DELUXE_SINGLE, RoomType.forRoomNumber(41));
        assertEquals(RoomType.DELUXE_SINGLE, RoomType.forRoomNumber(60));
    }

    @Test
    void roomNumberOutsideAllRangesThrows() {
        assertThrows(IllegalArgumentException.class, () -> RoomType.forRoomNumber(0));
        assertThrows(IllegalArgumentException.class, () -> RoomType.forRoomNumber(61));
    }
}
