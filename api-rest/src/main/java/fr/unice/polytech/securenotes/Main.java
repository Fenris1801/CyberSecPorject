import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) {
        var routing = new RoutingTable();

        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
            server.createContext("/api", new HttpProxyHandler(routing));
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("API Gateway listening on port 8080");
    }
}