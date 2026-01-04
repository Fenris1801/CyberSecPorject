package fr.unice.polytech.steats.api;

import java.util.Map;

public class RoutingTable {
    private final Map<String, String> routes = Map.of(
        "/api/auth", System.getenv("AUTH_API"),
        "/api/notes", System.getenv("NOTES_API"),
        "/api/health", System.getenv("HEALTH_API")
    );

    public String resolve(String path) {
        return routes.entrySet()
                .stream()
                .filter(e -> path.startsWith(e.getKey()))
                .map(Map.Entry::getValue)
                .findFirst().orElse(null);
    }
}