package fr.unice.polytech.securenotes.api.dto;

public record AuthResponse (
    String userId,
    String username,
    String token){}  