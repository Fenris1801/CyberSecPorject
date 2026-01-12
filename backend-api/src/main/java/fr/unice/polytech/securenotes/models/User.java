package fr.unice.polytech.securenotes.models;

import fr.unice.polytech.securenotes.replication.Replicable;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;
import java.time.LocalDateTime;

@Getter
public class User implements Replicable {
    @Setter
    private String id;
    @Setter
    private String username;
    @Setter
    private String passwordHash;
    @Setter
    private String email;
    @Setter
    private long version;

    private final LocalDateTime createdAt;
    
    public User() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.version = Instant.now().toEpochMilli();
    }
}
