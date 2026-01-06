package fr.unice.polytech.securenotes.api.controller;

import fr.unice.polytech.securenotes.models.User;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import fr.unice.polytech.securenotes.api.dto.RegisterRequest;
import fr.unice.polytech.securenotes.api.dto.LoginRequest;
import fr.unice.polytech.securenotes.api.dto.AuthResponse;
import fr.unice.polytech.securenotes.services.AuthService;

import java.io.IOException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        //TODO return AuthResponse
        AuthResponse response = authService.register(request.username(), request.password(), request.email());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    //TODO return AuthResponse
    public ResponseEntity<User> login(@RequestBody LoginRequest request) {
        User response = null;
        try {
            response = authService.login(request.username(), request.password());
        } catch (IOException e) {
            // TODO: renvoyer une erreur
            //return ResponseEntity.of();
        }
        return ResponseEntity.ok(response);
    }
};