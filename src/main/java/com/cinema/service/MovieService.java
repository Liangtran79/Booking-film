package com.cinema.service;

import com.cinema.entity.Category;
import com.cinema.entity.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MovieService {
    List<Movie> getAllMovies();
    Page<Movie> getMoviesPaginated(Pageable pageable);
    Optional<Movie> getMovieById(Integer id);
    List<Movie> getNowPlaying();
    List<Movie> getComingSoon();
    List<Movie> getRelatedMovies(Integer categoryId, Integer excludeId, int limit);
    List<Category> getAllCategories();
    Optional<Category> getCategoryById(Integer categoryId);

    Movie saveMovie(Movie movie, MultipartFile imageFile);
    Movie updateMovie(Integer id, String title, Integer categoryId, LocalDate releaseDate, String description, MultipartFile imageFile);
    void deleteMovie(Integer id);
    List<Movie> searchMovies(String keyword);
}
