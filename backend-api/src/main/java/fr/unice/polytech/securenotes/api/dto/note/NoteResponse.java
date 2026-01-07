package fr.unice.polytech.securenotes.api.dto.note;

import com.fasterxml.jackson.annotation.JsonProperty;
import fr.unice.polytech.securenotes.models.Share;

import java.time.LocalDateTime;
import java.util.List;

public record NoteResponse(
        @JsonProperty("id") String id,
        @JsonProperty("owner_id") String ownerId,
        @JsonProperty("title") String title,
        @JsonProperty("content") String content,
        @JsonProperty("created_at") LocalDateTime createdAt,
        @JsonProperty("updated_at") LocalDateTime updatedAt,
        @JsonProperty("last_modified_by") String lastModifiedBy,
        @JsonProperty("shares") List<Share> shares,
        @JsonProperty("locked_by") String lockedBy
) {}
