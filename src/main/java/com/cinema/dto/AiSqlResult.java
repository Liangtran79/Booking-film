package com.cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiSqlResult {
    private String question;
    private String sql;
    private String explanation;
    private List<String> columns;
    private List<Map<String, Object>> rows;
    private String error;
    private long elapsedMs;

    public boolean isSuccess() {
        return error == null || error.isBlank();
    }
}
