package fr.unice.polytech.securenotes.api.dto.note;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CreateNoteRequest(
        @JsonProperty("user_id") String userId,
        @JsonProperty("title") String title,
        @JsonProperty("content") String content
) {}
