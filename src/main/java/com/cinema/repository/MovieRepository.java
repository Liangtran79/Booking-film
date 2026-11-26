package com.cinema.repository;

import com.cinema.entity.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Integer> {

    // Phim đang chiếu
    List<Movie> findByReleaseDateLessThanEqualOrderByReleaseDateDesc(LocalDate date);

    // Phim sắp chiếu
    List<Movie> findByReleaseDateGreaterThanOrderByReleaseDateAsc(LocalDate date);

    // Phim cùng thể loại
    List<Movie> findByCategoryIdAndIdNot(Integer categoryId, Integer excludeId);

    List<Movie> findByCategoryId(Integer categoryId);

    // Tìm kiếm theo tên
    List<Movie> findByTitleContainingIgnoreCase(String keyword);

    Page<Movie> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);
}
