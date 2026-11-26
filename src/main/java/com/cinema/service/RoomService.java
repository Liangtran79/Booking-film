package com.cinema.service;

import com.cinema.entity.Room;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RoomService {
    List<Room> getAllRooms();
    Optional<Room> getRoomById(Integer id);
    Room saveRoom(Room room);
    Room updateRoom(Integer id, String name);
    void deleteRoom(Integer id);
    List<Room> getRoomsByMovieAndDate(Integer movieId, LocalDate date);
}
