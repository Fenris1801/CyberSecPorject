package fr.unice.polytech.securenotes.api.dto.note;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GetOwnerNameRequest(
        @JsonProperty("owner_id") String ownerId
) {}
