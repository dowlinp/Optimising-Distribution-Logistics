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

        // server.createContext("/api/delivery/", exchange -> {

        //     String orderId = exchange.getRequestURI()
        //                             .getPath()
        //                             .replace("/api/delivery/", "");

        //     // Run the Python route generator
        //     ProcessBuilder pb = new ProcessBuilder(
        //         "python3",
        //         "routing/generate_kml.py",
        //         orderId
        //     );

        //     pb.redirectErrorStream(true);

        //     Process process = pb.start();
        //     String output = new String(process.getInputStream().readAllBytes());

        //     int exitCode = process.waitFor();

        //     String response;

        //     if (exitCode == 0) {
        //         response = output;
        //     } else {
        //         exchange.sendResponseHeaders(500, 0);
        //         exchange.getResponseBody().write(output.getBytes());
        //         exchange.close();
        //         return;
        //     }

        //     byte[] body = response.getBytes();

        //     exchange.getResponseHeaders().set(
        //         "Content-Type", "text/plain; charset=utf-8"
        //     );

        //     exchange.sendResponseHeaders(200, body.length);
        //     exchange.getResponseBody().write(body);
        //     exchange.close();
        // });

        
        server.createContext("/routes/", exchange -> {

            String filename = exchange.getRequestURI().getPath().replace("/routes/", "");

            // Only allow GeoJSON files
            if (!filename.matches("[a-zA-Z0-9_-]+\\.geojson")) {
                exchange.sendResponseHeaders(400, -1);
                exchange.close();
                return;
            }

            Path routesFolder = Path.of("delivery/routes")
                                    .toAbsolutePath()
                                    .normalize();

            Path file = routesFolder.resolve(filename).normalize();

            if (!file.startsWith(routesFolder) || !Files.exists(file)) {
                exchange.sendResponseHeaders(404, -1);
                exchange.close();
                return;
            }

            byte[] body = Files.readAllBytes(file);

            exchange.getResponseHeaders().set(
                "Content-Type", "application/geo+json"
            );

            exchange.sendResponseHeaders(200, body.length);

            try (var output = exchange.getResponseBody()) {
                output.write(body);
            }
        });

        server.start();
        System.out.println("Running on http://" + host + ":5000");
    }
}