package fr.unice.polytech.securenotes.security;

import fr.unice.polytech.securenotes.models.Note;
import fr.unice.polytech.securenotes.models.User;
import fr.unice.polytech.securenotes.services.StorageService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class AuthService {

    private final JwtService jwtService;
    private final StorageService storageService;
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    public AuthService(JwtService jwtService, StorageService storageService) {
        this.storageService = storageService;
        this.jwtService = jwtService;
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

    public String generateToken(String userId, String username) {
        return jwtService.generateToken(userId, username);
    }

    public void saveReplica(User user) throws IOException {
        this.storageService.saveUser(user, false);
    }

    public List<User> getAll() throws IOException {
        return this.storageService.loadAllUsers();
    }
}
