package com.web.tech.controller;

import com.web.tech.config.MongoConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    private final MongoTemplate mongoTemplate;
    private final MongoConfig.MongoHealthIndicator healthIndicator;

    @Autowired
    public HealthController(MongoTemplate mongoTemplate,
                            MongoConfig.MongoHealthIndicator healthIndicator) {
        this.mongoTemplate = mongoTemplate;
        this.healthIndicator = healthIndicator;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("application", "TechStore");

        try {
            boolean isHealthy = healthIndicator.isHealthy();

            if (isHealthy) {
                // Get additional MongoDB info
                long userCount = mongoTemplate.getCollection("clients").estimatedDocumentCount();

                response.put("status", "UP");
                response.put("database", "MongoDB");
                response.put("connection", "OK");
                response.put("userCount", userCount);

                return ResponseEntity.ok(response);
            } else {
                response.put("status", "DOWN");
                response.put("database", "MongoDB");
                response.put("connection", "FAILED");
                response.put("error", "Database connection is not healthy");

                return ResponseEntity.status(503).body(response);
            }

        } catch (Exception e) {
            response.put("status", "DOWN");
            response.put("database", "MongoDB");
            response.put("connection", "ERROR");
            response.put("error", e.getMessage());

            return ResponseEntity.status(503).body(response);
        }
    }

    @GetMapping("/health/db")
    public ResponseEntity<Map<String, Object>> databaseHealth() {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());

        try {
            // Test basic connection
            mongoTemplate.getCollection("clients").estimatedDocumentCount();

            // Test write operation (optional)
            // You might want to have a "health_check" collection for this

            response.put("mongodb", "OK");
            response.put("collections", mongoTemplate.getCollectionNames());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("mongodb", "FAILED");
            response.put("error", e.getMessage());
            response.put("errorType", e.getClass().getSimpleName());

            return ResponseEntity.status(503).body(response);
        }
    }
}