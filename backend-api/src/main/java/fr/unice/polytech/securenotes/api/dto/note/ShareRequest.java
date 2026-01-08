package fr.unice.polytech.securenotes.api.dto.note;

import com.fasterxml.jackson.annotation.JsonProperty;
import fr.unice.polytech.securenotes.models.Share;

public record ShareRequest(
        @JsonProperty("note_id") String noteId,
        @JsonProperty("target_user_id") String targetUserId,
        @JsonProperty("permission") Share.SharePermission permission
) {}
