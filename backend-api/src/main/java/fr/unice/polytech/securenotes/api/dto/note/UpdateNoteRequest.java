package fr.unice.polytech.securenotes.api.dto.note;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UpdateNoteRequest(
        @JsonProperty("note_id") String noteId,
        @JsonProperty("title") String title,
        @JsonProperty("content") String content
) {}
