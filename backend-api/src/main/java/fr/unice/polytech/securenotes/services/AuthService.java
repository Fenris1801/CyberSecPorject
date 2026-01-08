package fr.unice.polytech.securenotes.services;

import fr.unice.polytech.securenotes.models.User;
import fr.unice.polytech.securenotes.services.StorageService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.Key;
import java.util.Date;

@Service
public class AuthService {
    
    private StorageService storageService;
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    public AuthService() {
        this.storageService = new StorageService();
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
        Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

        return Jwts.builder()
                .setSubject(userId)
                .claim("username", username)
                .setExpiration(new Date(System.currentTimeMillis() + 3600_000)) // 1h
                .signWith(key)
                .compact();
    }
}