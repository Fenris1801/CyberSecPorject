package fr.unice.polytech.securenotes.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AuthResponse(
        @JsonProperty("user_id")
        String userId,

        @JsonProperty("username")
        String username,

        @JsonProperty("token")
        String token
) {}