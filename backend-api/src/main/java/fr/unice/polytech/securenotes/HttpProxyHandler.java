package fr.unice.polytech.steats.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpProxyHandler implements HttpHandler {
    private final RoutingTable routing;

    public HttpProxyHandler(RoutingTable routing) {
        this.routing = routing;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        String target = routing.resolve(path);

        System.out.println("target: " + target);
        System.out.println("method: " + method);

        if (method.equalsIgnoreCase("OPTIONS")) {
            addCors(exchange);
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if (target == null) {
            addCors(exchange);
            exchange.sendResponseHeaders(404, -1);
            return;
        }

        URL url = new URL(target + path);
        System.out.println("url: " + url);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);

        exchange.getRequestHeaders().forEach((key, values) ->
                conn.setRequestProperty(key, String.join(",", values)));

        if (method.equals("POST") || method.equals("PUT") || method.equals("PATCH")) {
            conn.setDoOutput(true);

            try (InputStream is = exchange.getRequestBody();
                 OutputStream os = conn.getOutputStream()) {
                is.transferTo(os);
            }
        }

        InputStream upstreamStream;

        try {
            upstreamStream = conn.getInputStream();
        } catch (IOException e) {
            upstreamStream = conn.getErrorStream();
        }

        byte[] responseBytes = (upstreamStream != null)
                ? upstreamStream.readAllBytes()
                : new byte[0];

        addCors(exchange);

        conn.getHeaderFields().forEach((key, values) -> {
            if (key != null && values != null) {
                exchange.getResponseHeaders().put(key, values);
            }
        });

        exchange.sendResponseHeaders(conn.getResponseCode(), responseBytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }

        exchange.close();
    }

    private void addCors(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }
}