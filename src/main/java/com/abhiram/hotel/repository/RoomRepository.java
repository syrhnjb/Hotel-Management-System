package com.abhiram.hotel.repository;

import com.abhiram.hotel.domain.Room;
import com.abhiram.hotel.domain.RoomType;
import java.util.List;
import java.util.Optional;

/** Read/write access to the hotel's fixed set of {@link Room}s. */
public interface RoomRepository {

    Optional<Room> findByRoomNumber(int roomNumber);

    List<Room> findByType(RoomType type);

    List<Room> findAll();
}
