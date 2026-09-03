package com.abhiram.hotel.service;

import com.abhiram.hotel.domain.Room;
import com.abhiram.hotel.domain.RoomType;
import com.abhiram.hotel.exception.RoomNotFoundException;
import com.abhiram.hotel.repository.RoomRepository;
import java.util.List;

/** Read-oriented operations on the hotel's room inventory: lookups and availability queries. */
public final class RoomService {

    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public Room getRoom(int roomNumber) {
        return roomRepository.findByRoomNumber(roomNumber)
                .orElseThrow(() -> new RoomNotFoundException(roomNumber));
    }

    public List<Room> availableRoomsOfType(RoomType type) {
        return roomRepository.findByType(type).stream()
                .filter(Room::isAvailable)
                .toList();
    }

    public int countAvailable(RoomType type) {
        return availableRoomsOfType(type).size();
    }
}
