package fr.unice.polytech.securenotes.api.dto.note;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GetOwnerNameResponse(
        @JsonProperty("owner_name") String ownerName
) {}
