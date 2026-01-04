package fr.unice.polytech.securenotes.services;

import fr.unice.polytech.securenotes.models.User;
import fr.unice.polytech.securenotes.storage.FileStorageService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class AuthService {
    
    private StorageService storageService;
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    public NoteService() {
        this.storageService = new FileStorageService();
    }

    public User register(String username, String password, String email) throws IOException {
        User existingUser = storageService.findUserByUsername(username);
        if (existingUser != null) {
            throw new IllegalArgumentException("Nom d'utilisateur déjà pris");
        }
        
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setEmail(email);
        
        storageService.saveUser(user);
        return user;
    }
    
    public User login(String username, String password) throws IOException {
        User user = storageService.findUserByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("Identifiants invalides");
        }
        
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Identifiants invalides");
        }
        
        return user;
    }
}