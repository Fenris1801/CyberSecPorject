package fr.unice.polytech.securenotes.api.dto.note;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RevokeShareRequest(
        @JsonProperty("note_id") String noteId,
        @JsonProperty("target_user_id") String targetUserId
) {}
