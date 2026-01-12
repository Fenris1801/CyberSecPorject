package fr.unice.polytech.securenotes.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fr.unice.polytech.securenotes.models.Note;
import fr.unice.polytech.securenotes.models.User;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class StorageService {
    @Value("${DATA_DIR:/data}")
    private String STORAGE_DIR;

    private final String usersDir = "/data/users";
    private final String notesDir = "/data/notes";

    private final ObjectMapper objectMapper;

    public StorageService(ObjectMapper mapper) {
        this.objectMapper = mapper;
    }

    @PostConstruct
    private void init() {
        try {
            Files.createDirectories(Path.of(usersDir));
            Files.createDirectories(Path.of(notesDir));
        } catch (IOException e) {
            throw new RuntimeException("Impossible de créer les répertoires de stockage", e);
        }
    }

    public void saveUser(User user, boolean updateTimestamp) throws IOException {
        if (updateTimestamp) user.setVersion(Instant.now().toEpochMilli());
        Path filePath = Path.of(usersDir).resolve(user.getId() + ".json");
        objectMapper.writeValue(filePath.toFile(), user);
    }

    public void saveUser(User user) throws IOException {
        saveUser(user, true);
    }

    public User loadUser(String userId) throws IOException {
        Path filePath = Path.of(usersDir).resolve(userId + ".json");
        if (!Files.exists(filePath)) return null;
        return objectMapper.readValue(filePath.toFile(), User.class);
    }

    public User findUserByUsername(String username) throws IOException {
        File[] userFiles = Path.of(usersDir).toFile().listFiles((dir, name) -> name.endsWith(".json"));
        if (userFiles == null) return null;

        for (File file : userFiles) {
            User user = objectMapper.readValue(file, User.class);
            if (user.getUsername().equals(username)) return user;
        }
        return null;
    }

    public void saveNote(Note note, boolean updateTimestamp) throws IOException {
        if (updateTimestamp) note.setVersion(Instant.now().toEpochMilli());
        Path filePath = Path.of(notesDir).resolve(note.getId() + ".json");
        objectMapper.writeValue(filePath.toFile(), note);
    }

    public void saveNote(Note note) throws IOException {
        saveNote(note, true);
    }

    public Note loadNote(String noteId) throws IOException {
        Path filePath = Path.of(notesDir).resolve(noteId + ".json");
        if (!Files.exists(filePath)) return null;
        return objectMapper.readValue(filePath.toFile(), Note.class);
    }

    public void deleteNote(String noteId) throws IOException {
        Path filePath = Path.of(notesDir).resolve(noteId + ".json");
        Files.deleteIfExists(filePath);
    }

    public List<Note> loadUserNotes(String userId) throws IOException {
        List<Note> notes = new ArrayList<>();
        File[] noteFiles = Path.of(notesDir).toFile().listFiles((dir, name) -> name.endsWith(".json"));
        if (noteFiles == null) return notes;

        for (File file : noteFiles) {
            Note note = objectMapper.readValue(file, Note.class);
            if (note.getOwnerId().equals(userId)) notes.add(note);
        }
        return notes;
    }

    public List<Note> loadSharedNotes(String userId) throws IOException {
        List<Note> shared = new ArrayList<>();
        File[] noteFiles = Path.of(notesDir).toFile().listFiles((dir, name) -> name.endsWith(".json"));
        if (noteFiles == null) return shared;

        for (File file : noteFiles) {
            Note note = objectMapper.readValue(file, Note.class);
            if (note.getShares().stream().anyMatch(s -> s.getUserId().equals(userId))) shared.add(note);
        }
        return shared;
    }

    public List<Note> loadAllNotes() throws IOException {
        List<Note> notes = new ArrayList<>();
        File[] noteFiles = Path.of(notesDir).toFile().listFiles((dir, name) -> name.endsWith(".json"));
        if (noteFiles == null) return notes;

        for (File file : noteFiles) {
            notes.add(objectMapper.readValue(file, Note.class));
        }
        return notes;
    }

    public List<User> loadAllUsers() throws IOException {
        List<User> users = new ArrayList<>();
        File[] userFiles = Path.of(usersDir).toFile().listFiles((dir, name) -> name.endsWith(".json"));
        if (userFiles == null) return users;

        for (File file : userFiles) {
            users.add(objectMapper.readValue(file, User.class));
        }
        return users;
    }
}
