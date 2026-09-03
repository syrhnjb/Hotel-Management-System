package com.abhiram.hotel.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.abhiram.hotel.domain.RoomType;
import com.abhiram.hotel.exception.RoomNotFoundException;
import com.abhiram.hotel.repository.InMemoryRoomRepository;
import com.abhiram.hotel.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RoomServiceTest {

    private RoomService roomService;

    @BeforeEach
    void setUp() {
        RoomRepository roomRepository = new InMemoryRoomRepository();
        roomService = new RoomService(roomRepository);
    }

    @Test
    void allRoomsOfEachTypeStartAvailable() {
        assertEquals(10, roomService.countAvailable(RoomType.LUXURY_DOUBLE));
        assertEquals(20, roomService.countAvailable(RoomType.DELUXE_DOUBLE));
        assertEquals(10, roomService.countAvailable(RoomType.LUXURY_SINGLE));
        assertEquals(20, roomService.countAvailable(RoomType.DELUXE_SINGLE));
    }

    @Test
    void gettingANonExistentRoomThrows() {
        assertThrows(RoomNotFoundException.class, () -> roomService.getRoom(9999));
    }

    @Test
    void gettingAValidRoomReturnsItsCorrectType() {
        assertEquals(RoomType.LUXURY_DOUBLE, roomService.getRoom(1).roomType());
        assertEquals(RoomType.DELUXE_DOUBLE, roomService.getRoom(11).roomType());
        assertEquals(RoomType.LUXURY_SINGLE, roomService.getRoom(31).roomType());
        assertEquals(RoomType.DELUXE_SINGLE, roomService.getRoom(60).roomType());
    }
}
