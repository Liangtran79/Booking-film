package com.cinema.service.impl;

import com.cinema.entity.Category;
import com.cinema.entity.Movie;
import com.cinema.repository.CategoryRepository;
import com.cinema.repository.MovieRepository;
import com.cinema.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;
    private final CategoryRepository categoryRepository;

    private static final String UPLOAD_DIR = "uploads/";

    @Override
    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    @Override
    public Page<Movie> getMoviesPaginated(Pageable pageable) {
        return movieRepository.findAll(pageable);
    }

    @Override
    public Optional<Movie> getMovieById(Integer id) {
        return movieRepository.findById(id);
    }

    @Override
    public List<Movie> getNowPlaying() {
        return movieRepository.findByReleaseDateLessThanEqualOrderByReleaseDateDesc(LocalDate.now());
    }

    @Override
    public List<Movie> getComingSoon() {
        return movieRepository.findByReleaseDateGreaterThanOrderByReleaseDateAsc(LocalDate.now());
    }

    @Override
    public List<Movie> getRelatedMovies(Integer categoryId, Integer excludeId, int limit) {
        List<Movie> list = movieRepository.findByCategoryIdAndIdNot(categoryId, excludeId);
        Collections.shuffle(list);
        return list.stream().limit(limit).toList();
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public Optional<Category> getCategoryById(Integer categoryId) {
        return categoryRepository.findById(categoryId);
    }

    @Override
    @Transactional
    public Movie saveMovie(Movie movie, MultipartFile imageFile) {
        if (imageFile != null && !imageFile.isEmpty()) {
            String savedFileName = saveUploadedFile(imageFile);
            movie.setImage(savedFileName);
        }
        return movieRepository.save(movie);
    }

    @Override
    @Transactional
    public Movie updateMovie(Integer id, String title, Integer categoryId, LocalDate releaseDate, String description, MultipartFile imageFile) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phim ID: " + id));

        movie.setTitle(title);
        if (categoryId != null) {
            Category cat = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new IllegalArgumentException("Thể loại không hợp lệ: " + categoryId));
            movie.setCategory(cat);
        }
        movie.setReleaseDate(releaseDate);
        movie.setDescription(description);

        if (imageFile != null && !imageFile.isEmpty()) {
            String savedFileName = saveUploadedFile(imageFile);
            movie.setImage(savedFileName);
        }

        return movieRepository.save(movie);
    }

    @Override
    @Transactional
    public void deleteMovie(Integer id) {
        movieRepository.deleteById(id);
    }

    @Override
    public List<Movie> searchMovies(String keyword) {
        return movieRepository.findByTitleContainingIgnoreCase(keyword);
    }

    private String saveUploadedFile(MultipartFile file) {
        try {
            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID().toString().replace("-", "") + extension;
            Path targetPath = Paths.get(UPLOAD_DIR).resolve(newFilename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            return newFilename;
        } catch (IOException e) {
            throw new RuntimeException("Lỗi lưu trữ hình ảnh tải lên", e);
        }
    }
}
