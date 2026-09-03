package com.abhiram.hotel.repository;

import com.abhiram.hotel.domain.Room;
import com.abhiram.hotel.domain.RoomType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory {@link RoomRepository}. The hotel's room inventory (which room numbers exist, and
 * what type each is) is fixed at construction time from {@link RoomType}'s ranges--rooms are
 * never created or destroyed at runtime, only their {@code status} changes.
 */
public final class InMemoryRoomRepository implements RoomRepository {

    private final Map<Integer, Room> roomsByNumber = new LinkedHashMap<>();

    public InMemoryRoomRepository() {
        for (RoomType type : RoomType.values()) {
            for (int roomNumber = type.firstRoomNumber(); roomNumber <= type.lastRoomNumber(); roomNumber++) {
                roomsByNumber.put(roomNumber, new Room(roomNumber, type));
            }
        }
    }

    @Override
    public Optional<Room> findByRoomNumber(int roomNumber) {
        return Optional.ofNullable(roomsByNumber.get(roomNumber));
    }

    @Override
    public List<Room> findByType(RoomType type) {
        List<Room> result = new ArrayList<>();
        for (Room room : roomsByNumber.values()) {
            if (room.roomType() == type) {
                result.add(room);
            }
        }
        return result;
    }

    @Override
    public List<Room> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(roomsByNumber.values()));
    }
}
