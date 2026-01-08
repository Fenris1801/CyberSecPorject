package fr.unice.polytech.securenotes.api.dto.note;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NoteRequest(
        @JsonProperty("note_id") String noteId
) {}
