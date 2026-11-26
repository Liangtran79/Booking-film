package com.cinema.controller.client;

import com.cinema.entity.Movie;
import com.cinema.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final MovieService movieService;

    @GetMapping({"/", "/home", "/index", "/index.php"})
    public String index(Model model) {
        List<Movie> nowPlaying = movieService.getNowPlaying();
        List<Movie> comingSoon = movieService.getComingSoon();

        model.addAttribute("nowPlayingMovies", nowPlaying);
        model.addAttribute("comingSoonMovies", comingSoon);
        return "client/index";
    }
}
