package fr.unice.polytech.securenotes.models;

import fr.unice.polytech.securenotes.replication.Replicable;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;

@Getter
public class Note implements Replicable {
    @Setter
    private String id;
    @Setter
    private String ownerId;
    @Setter
    private String title;
    @Setter
    private String content;

    private final LocalDateTime createdAt;
    @Setter
    private LocalDateTime updatedAt;
    @Setter
    private String lastModifiedBy;
    @Setter
    private List<Share> shares;
    @Setter
    private String lockedBy;
    @Setter
    private long version;

    public Note() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.shares = new ArrayList<>();
        this.version = Instant.now().toEpochMilli();
    }
}
