package fr.unice.polytech.securenotes.models;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;
import java.time.LocalDateTime;

@Getter
public class User {
    @Setter
    private String id;
    @Setter
    private String username;
    @Setter
    private String passwordHash;
    @Setter
    private String email;

    private final LocalDateTime createdAt;
    
    public User() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
    }
}