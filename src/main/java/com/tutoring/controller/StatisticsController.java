package com.tutoring.controller;

import com.tutoring.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @GetMapping("/general")
    public ResponseEntity<?> getGeneralStatistics() {
        try {
            Map<String, Object> stats = statisticsService.getGeneralStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка при получении статистики");
        }
    }

    @GetMapping("/subjects/popularity")
    public ResponseEntity<?> getSubjectPopularity() {
        try {
            Map<String, Long> popularity = statisticsService.getSubjectPopularity();
            return ResponseEntity.ok(popularity);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка при получении популярности предметов");
        }
    }
}
