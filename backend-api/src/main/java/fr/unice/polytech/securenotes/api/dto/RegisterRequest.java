package fr.unice.polytech.securenotes.api.dto;

public record RegisterRequest (
    String username,
    String password,
    String email){}