package com.cinema.repository;

import com.cinema.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Integer> {

    @Query("SELECT DISTINCT s.room FROM Showtime s WHERE s.movie.id = :movieId AND s.showDate = :date")
    List<Room> findRoomsByMovieAndDate(@Param("movieId") Integer movieId, @Param("date") LocalDate date);
}
