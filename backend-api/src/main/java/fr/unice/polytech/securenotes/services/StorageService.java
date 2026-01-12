package fr.unice.polytech.securenotes.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fr.unice.polytech.securenotes.models.Note;
import fr.unice.polytech.securenotes.models.User;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class StorageService {
    private final String STORAGE_DIR = "storage";
    private final String USERS_DIR = STORAGE_DIR + "/users";
    private final String NOTES_DIR = STORAGE_DIR + "/notes";
    private final ObjectMapper objectMapper;
    
    public StorageService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        initializeDirectories();
    }
    
    private void initializeDirectories() {
        try {
            Files.createDirectories(Paths.get(USERS_DIR));
            Files.createDirectories(Paths.get(NOTES_DIR));
        } catch (IOException e) {
            throw new RuntimeException("Impossible de créer les répertoires de stockage", e);
        }
    }
    
    public void saveUser(User user) throws IOException {
        Path filePath = Paths.get(USERS_DIR, user.getId() + ".json");
        objectMapper.writeValue(filePath.toFile(), user);
    }
    
    public User loadUser(String userId) throws IOException {
        Path filePath = Paths.get(USERS_DIR, userId + ".json");
        if (!Files.exists(filePath)) {
            return null;
        }
        return objectMapper.readValue(filePath.toFile(), User.class);
    }
    
    public User findUserByUsername(String username) throws IOException {
        File[] userFiles = new File(USERS_DIR).listFiles((dir, name) -> name.endsWith(".json"));
        if (userFiles == null) return null;
        
        for (File file : userFiles) {
            User user = objectMapper.readValue(file, User.class);
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }
    
    public void saveNote(Note note) throws IOException {
        Path filePath = Paths.get(NOTES_DIR, note.getId() + ".json");
        objectMapper.writeValue(filePath.toFile(), note);
    }
    
    public Note loadNote(String noteId) throws IOException {
        System.out.println(noteId);
        System.out.println(Paths.get(NOTES_DIR, noteId + ".json"));
        System.out.println(new File(NOTES_DIR + "/" + noteId + ".json").exists());
        Path filePath = Paths.get(NOTES_DIR, noteId + ".json");
        if (!Files.exists(filePath)) {
            return null;
        }
        return objectMapper.readValue(filePath.toFile(), Note.class);
    }
    
    public void deleteNote(String noteId) throws IOException {
        Path filePath = Paths.get(NOTES_DIR, noteId + ".json");
        Files.deleteIfExists(filePath);
    }
    
    public List<Note> loadUserNotes(String userId) throws IOException {
        List<Note> userNotes = new ArrayList<>();
        File[] noteFiles = new File(NOTES_DIR).listFiles((dir, name) -> name.endsWith(".json"));
        
        if (noteFiles == null) return userNotes;
        
        for (File file : noteFiles) {
            Note note = objectMapper.readValue(file, Note.class);
            if (note.getOwnerId().equals(userId)) {
                userNotes.add(note);
            }
        }
        return userNotes;
    }
    
    public List<Note> loadSharedNotes(String userId) throws IOException {
        List<Note> sharedNotes = new ArrayList<>();
        File[] noteFiles = new File(NOTES_DIR).listFiles((dir, name) -> name.endsWith(".json"));
        
        if (noteFiles == null) return sharedNotes;
        
        for (File file : noteFiles) {
            Note note = objectMapper.readValue(file, Note.class);
            if (note.getShares().stream().anyMatch(share -> share.getUserId().equals(userId))) {
                sharedNotes.add(note);
            }
        }
        return sharedNotes;
    }
}