package fr.unice.polytech.securenotes.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record HealthResponse(
        @JsonProperty("status")
        String status,

        @JsonProperty("server_time")
        String serverTime
) {}