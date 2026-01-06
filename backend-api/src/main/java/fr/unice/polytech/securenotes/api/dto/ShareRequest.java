package fr.unice.polytech.securenotes.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ShareRequest (
        @JsonProperty("targetUserId")
    String targetUserId,

        @JsonProperty("permission")
    String permission){}