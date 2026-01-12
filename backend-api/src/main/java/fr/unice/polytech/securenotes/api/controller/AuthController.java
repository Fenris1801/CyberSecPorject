package fr.unice.polytech.securenotes.api.controller;

import fr.unice.polytech.securenotes.models.User;
import fr.unice.polytech.securenotes.replication.ReplicationEntityType;
import fr.unice.polytech.securenotes.replication.ReplicationJournal;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import fr.unice.polytech.securenotes.api.dto.RegisterRequest;
import fr.unice.polytech.securenotes.api.dto.LoginRequest;
import fr.unice.polytech.securenotes.api.dto.AuthResponse;
import fr.unice.polytech.securenotes.security.AuthService;

import java.io.IOException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private AuthService authService;
    private ReplicationJournal journal;

    public AuthController(AuthService authService, ReplicationJournal journal) {
        this.authService = authService;
        this.journal = journal;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        User user;
        try {
            user = authService.register(request.username(), request.password(), request.email());
        }
        catch (IOException e) {  // problème au register
            return ResponseEntity.badRequest().build();
        }
        if (user != null) {
            String token = authService.generateToken(user.getId(), user.getUsername());
            AuthResponse response = new AuthResponse(user.getId(), user.getUsername(), token);

            try {
                this.journal.add(ReplicationEntityType.USER, user, "create");
            } catch (IOException e) {
                System.out.println(e.getMessage());
                return ResponseEntity.internalServerError().build();
            }

            return ResponseEntity.ok(response);
        }

        // si user null
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        User user;
        try {
            user = authService.login(request.username(), request.password());
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }
        if (user != null) {
            String token = authService.generateToken(user.getId(), user.getUsername());
            AuthResponse response = new AuthResponse(user.getId(), user.getUsername(), token);

            try {
                this.journal.add(ReplicationEntityType.USER, user, "update");
            } catch (IOException e) {
                System.out.println(e.getMessage());
                return ResponseEntity.internalServerError().build();
            }

            return ResponseEntity.ok(response);
        }

        return ResponseEntity.badRequest().build();
    }
}
