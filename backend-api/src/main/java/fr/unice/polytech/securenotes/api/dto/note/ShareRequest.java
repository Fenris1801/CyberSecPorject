package fr.unice.polytech.securenotes.api.dto.note;

import com.fasterxml.jackson.annotation.JsonProperty;
import fr.unice.polytech.securenotes.models.Share;

public record ShareRequest(
        @JsonProperty("noteId") String noteId,
        @JsonProperty("targetUserId") String targetUserId,
        @JsonProperty("permission") String permission
) {}
