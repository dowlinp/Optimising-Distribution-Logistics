import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;

public class App {
    public static void main(String[] args) throws Exception {
        // 0.0.0.0 so it is reachable from outside a Docker container
        String host = System.getenv().getOrDefault("HOST", "127.0.0.1");
        HttpServer server =
                HttpServer.create(new InetSocketAddress(host, 5000), 0);

        // "/" serves Page.html
        server.createContext("/", exchange -> {
            byte[] body = Files.readAllBytes(Path.of("home.html"));
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });

        server.createContext("/User", exchange -> {
            byte[] body = Files.readAllBytes(Path.of("frontend/frontend.html"));
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });

        server.createContext("/Warehouse", exchange -> {
            byte[] body = Files.readAllBytes(Path.of("warehouse/warehouse.html"));
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });

        // "/delivery" serves delivery.html
        server.createContext("/Delivery", exchange -> {
            byte[] body = Files.readAllBytes(Path.of("delivery/delivery.html"));
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });

        server.createContext("/videos/", exchange -> {
            Path videoPath = Path.of("videos/CityMap2.png");

            byte[] video = Files.readAllBytes(videoPath);

            exchange.getResponseHeaders().set("Content-Type", "video/mp4");
            exchange.sendResponseHeaders(200, video.length);

            try (var output = exchange.getResponseBody()) {
                output.write(video);
            }
        });

        server.start();
        System.out.println("Running on http://" + host + ":5000");
    }
}