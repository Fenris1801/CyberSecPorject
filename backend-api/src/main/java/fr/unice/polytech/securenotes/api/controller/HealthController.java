package fr.unice.polytech.securenotes.api.controller;

import fr.unice.polytech.securenotes.api.dto.HealthResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
public class HealthController {

    @GetMapping("/api/health")
    public ResponseEntity<HealthResponse> getHealth() {
        HealthResponse health = new HealthResponse("UP", LocalDateTime.now().toString());
        return ResponseEntity.ok(health);
    }
}
