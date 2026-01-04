package fr.unice.polytech.securenotes.api.dto;

public record NoteRequest (
    String title,
    String content){}