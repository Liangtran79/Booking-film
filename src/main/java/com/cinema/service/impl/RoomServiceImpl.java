package com.cinema.service.impl;

import com.cinema.entity.Room;
import com.cinema.repository.RoomRepository;
import com.cinema.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;

    @Override
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    @Override
    public Optional<Room> getRoomById(Integer id) {
        return roomRepository.findById(id);
    }

    @Override
    @Transactional
    public Room saveRoom(Room room) {
        return roomRepository.save(room);
    }

    @Override
    @Transactional
    public Room updateRoom(Integer id, String name) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng ID: " + id));
        room.setName(name);
        return roomRepository.save(room);
    }

    @Override
    @Transactional
    public void deleteRoom(Integer id) {
        roomRepository.deleteById(id);
    }

    @Override
    public List<Room> getRoomsByMovieAndDate(Integer movieId, LocalDate date) {
        return roomRepository.findRoomsByMovieAndDate(movieId, date);
    }
}
