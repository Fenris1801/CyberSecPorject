package fr.unice.polytech.securenotes.api.dto.note;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CreateNoteRequest(
        @JsonProperty("title") String title,
        @JsonProperty("content") String content
) {}
