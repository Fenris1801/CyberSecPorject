package fr.unice.polytech.securenotes.api.dto;

public record ShareRequest (
    String targetUserId,
    String permission){}