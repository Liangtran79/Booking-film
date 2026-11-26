package com.cinema.service;

import com.cinema.dto.AiSqlResult;
import com.cinema.entity.Movie;

public interface OllamaService {
    boolean isAvailable();
    String getConfiguredModel();
    String chat(String systemPrompt, String userPrompt, boolean jsonFormat);
    AiSqlResult queryDatabaseWithAi(String question);
    String generateMoviePromo(Movie movie);
}
