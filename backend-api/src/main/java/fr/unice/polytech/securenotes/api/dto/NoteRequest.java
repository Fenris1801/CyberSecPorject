package fr.unice.polytech.securenotes.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NoteRequest (
        @JsonProperty("title")
    String title,

    @JsonProperty("content")
    String content){}