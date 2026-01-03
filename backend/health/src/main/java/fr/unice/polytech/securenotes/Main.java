package fr.unice.polytech.steats.health;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8083), 0);

        server.createContext("/api/health", new HealthHandler());

        server.start();
        System.out.println("Health service listening on port 8083");
    }
}