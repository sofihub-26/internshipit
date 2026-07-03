import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BackendServer {

    private static final Path ROOT = Paths.get(System.getProperty("user.dir"));
    private static final Path DATA_DIR = ROOT.resolve("data");

    public static void main(String[] args) throws Exception {
        if (!Files.exists(DATA_DIR)) {
            Files.createDirectories(DATA_DIR);
        }
        ensureDataFile("clients.json");
        ensureDataFile("estimates.json");
        ensureDataFile("invoices.json");
        ensureDataFile("payments.json");

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/api/clients", new ClientsHandler());
        server.createContext("/api/estimates", new EstimatesHandler());
        server.createContext("/api/invoices", new InvoicesHandler());
        server.createContext("/api/payments", new PaymentsHandler());
        server.createContext("/api/summary", new SummaryHandler());
        server.createContext("/", new StaticHandler());
        server.setExecutor(null);
        System.out.println("Backend running on http://localhost:8080");
        server.start();
    }

    private static void ensureDataFile(String fileName) throws IOException {
        Path path = DATA_DIR.resolve(fileName);
        if (!Files.exists(path)) {
            Files.writeString(path, "[]", StandardCharsets.UTF_8);
        }
    }

    static class ClientsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            try {
                String method = exchange.getRequestMethod();
                if ("GET".equalsIgnoreCase(method)) {
                    handleGet(exchange);
                } else if ("POST".equalsIgnoreCase(method)) {
                    handlePost(exchange);
                } else if ("DELETE".equalsIgnoreCase(method)) {
                    handleDelete(exchange);
                } else {
                    sendJson(exchange, 405, "{\"error\": \"Method not allowed\"}");
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendJson(exchange, 500, "{\"error\": \"Server error\"}");
            }
        }

        private void handleGet(HttpExchange exchange) throws IOException {
            try {
                List<Map<String, Object>> clients = readData("clients.json");
                sendJson(exchange, 200, JsonUtil.toJson(clients));
            } catch (Exception ex) {
                ex.printStackTrace();
                sendJson(exchange, 500, "{\"error\": \"Unable to query clients\"}");
            }
        }

        private void handlePost(HttpExchange exchange) throws IOException {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> data = JsonUtil.fromJson(body);
            try {
                List<Map<String, Object>> clients = readData("clients.json");
                int nextId = nextId(clients);
                Map<String, Object> row = new HashMap<>();
                row.put("id", nextId);
                row.put("name", data.getOrDefault("name", ""));
                row.put("phone", data.getOrDefault("phone", ""));
                row.put("email", data.getOrDefault("email", ""));
                row.put("group", data.getOrDefault("group", ""));
                row.put("chain", data.getOrDefault("chain", ""));
                row.put("brand", data.getOrDefault("brand", ""));
                row.put("address", data.getOrDefault("address", ""));
                clients.add(row);
                writeData("clients.json", clients);
                sendJson(exchange, 201, "{\"status\": \"ok\"}");
            } catch (Exception ex) {
                ex.printStackTrace();
                sendJson(exchange, 500, "{\"error\": \"Unable to save client\"}");
            }
        }

        private void handleDelete(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            int id = QueryUtil.getInt(query, "id", -1);
            if (id <= 0) {
                sendJson(exchange, 400, "{\"error\": \"Missing id\"}");
                return;
            }
            try {
                List<Map<String, Object>> clients = readData("clients.json");
                clients.removeIf(item -> asInt(item.get("id")) == id);
                writeData("clients.json", clients);
                sendJson(exchange, 200, "{\"status\": \"deleted\"}");
            } catch (Exception ex) {
                ex.printStackTrace();
                sendJson(exchange, 500, "{\"error\": \"Unable to delete client\"}");
            }
        }
    }

    static class EstimatesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            try {
                String method = exchange.getRequestMethod();
                if ("GET".equalsIgnoreCase(method)) {
                    handleGet(exchange);
                } else if ("POST".equalsIgnoreCase(method)) {
                    handlePost(exchange);
                } else if ("DELETE".equalsIgnoreCase(method)) {
                    handleDelete(exchange);
                } else {
                    sendJson(exchange, 405, "{\"error\": \"Method not allowed\"}");
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendJson(exchange, 500, "{\"error\": \"Server error\"}");
            }
        }

        private void handleGet(HttpExchange exchange) throws IOException {
            try {
                List<Map<String, Object>> estimates = readData("estimates.json");
                sendJson(exchange, 200, JsonUtil.toJson(estimates));
            } catch (Exception ex) {
                ex.printStackTrace();
                sendJson(exchange, 500, "{\"error\": \"Unable to query estimates\"}");
            }
        }

        private void handlePost(HttpExchange exchange) throws IOException {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> data = JsonUtil.fromJson(body);
            try {
                List<Map<String, Object>> estimates = readData("estimates.json");
                int nextId = nextId(estimates);
                Map<String, Object> row = new HashMap<>();
                row.put("id", nextId);
                row.put("clientName", data.getOrDefault("clientName", ""));
                row.put("date", data.getOrDefault("date", ""));
                row.put("amount", parseDouble(data.getOrDefault("amount", "0")));
                row.put("gst", parseDouble(data.getOrDefault("gst", "0")));
                row.put("total", parseDouble(data.getOrDefault("total", "0")));
                row.put("status", data.getOrDefault("status", "Pending"));
                row.put("remarks", data.getOrDefault("remarks", ""));
                estimates.add(row);
                writeData("estimates.json", estimates);
                sendJson(exchange, 201, "{\"status\": \"ok\"}");
            } catch (Exception ex) {
                ex.printStackTrace();
                sendJson(exchange, 500, "{\"error\": \"Unable to save estimate\"}");
            }
        }

        private void handleDelete(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            int id = QueryUtil.getInt(query, "id", -1);
            if (id <= 0) {
                sendJson(exchange, 400, "{\"error\": \"Missing id\"}");
                return;
            }
            try {
                List<Map<String, Object>> estimates = readData("estimates.json");
                estimates.removeIf(item -> asInt(item.get("id")) == id);
                writeData("estimates.json", estimates);
                sendJson(exchange, 200, "{\"status\": \"deleted\"}");
            } catch (Exception ex) {
                ex.printStackTrace();
                sendJson(exchange, 500, "{\"error\": \"Unable to delete estimate\"}");
            }
        }

        private double parseDouble(String text) {
            try {
                return Double.parseDouble(text);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
    }

    static class InvoicesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            try {
                String method = exchange.getRequestMethod();
                if ("GET".equalsIgnoreCase(method)) {
                    handleGet(exchange);
                } else if ("POST".equalsIgnoreCase(method)) {
                    handlePost(exchange);
                } else if ("DELETE".equalsIgnoreCase(method)) {
                    handleDelete(exchange);
                } else {
                    sendJson(exchange, 405, "{\"error\": \"Method not allowed\"}");
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendJson(exchange, 500, "{\"error\": \"Server error\"}");
            }
        }

        private void handleGet(HttpExchange exchange) throws IOException {
            try {
                List<Map<String, Object>> invoices = readData("invoices.json");
                sendJson(exchange, 200, JsonUtil.toJson(invoices));
            } catch (Exception ex) {
                ex.printStackTrace();
                sendJson(exchange, 500, "{\"error\": \"Unable to query invoices\"}");
            }
        }

        private void handlePost(HttpExchange exchange) throws IOException {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> data = JsonUtil.fromJson(body);
            try {
                List<Map<String, Object>> invoices = readData("invoices.json");
                int nextId = nextId(invoices);
                Map<String, Object> row = new HashMap<>();
                row.put("id", nextId);
                row.put("invoiceNo", data.getOrDefault("invoiceNo", ""));
                row.put("estimateId", parseInt(data.getOrDefault("estimateId", "0")));
                row.put("clientName", data.getOrDefault("clientName", ""));
                row.put("invoiceDate", data.getOrDefault("invoiceDate", ""));
                row.put("amount", parseDouble(data.getOrDefault("amount", "0")));
                row.put("gst", parseDouble(data.getOrDefault("gst", "0")));
                row.put("total", parseDouble(data.getOrDefault("total", "0")));
                row.put("status", data.getOrDefault("status", "Pending"));
                invoices.add(row);
                writeData("invoices.json", invoices);
                sendJson(exchange, 201, "{\"status\": \"ok\"}");
            } catch (Exception ex) {
                ex.printStackTrace();
                sendJson(exchange, 500, "{\"error\": \"Unable to save invoice\"}");
            }
        }

        private void handleDelete(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            String invoiceNo = QueryUtil.getString(query, "invoiceNo", "");
            if (invoiceNo.isEmpty()) {
                sendJson(exchange, 400, "{\"error\": \"Missing invoiceNo\"}");
                return;
            }
            try {
                List<Map<String, Object>> invoices = readData("invoices.json");
                invoices.removeIf(item -> invoiceNo.equals(item.get("invoiceNo")));
                writeData("invoices.json", invoices);
                sendJson(exchange, 200, "{\"status\": \"deleted\"}");
            } catch (Exception ex) {
                ex.printStackTrace();
                sendJson(exchange, 500, "{\"error\": \"Unable to delete invoice\"}");
            }
        }
    }

    static class PaymentsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            try {
                String method = exchange.getRequestMethod();
                if ("GET".equalsIgnoreCase(method)) {
                    handleGet(exchange);
                } else if ("POST".equalsIgnoreCase(method)) {
                    handlePost(exchange);
                } else if ("DELETE".equalsIgnoreCase(method)) {
                    handleDelete(exchange);
                } else {
                    sendJson(exchange, 405, "{\"error\": \"Method not allowed\"}");
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendJson(exchange, 500, "{\"error\": \"Server error\"}");
            }
        }

        private void handleGet(HttpExchange exchange) throws IOException {
            try {
                List<Map<String, Object>> payments = readData("payments.json");
                sendJson(exchange, 200, JsonUtil.toJson(payments));
            } catch (Exception ex) {
                ex.printStackTrace();
                sendJson(exchange, 500, "{\"error\": \"Unable to query payments\"}");
            }
        }

        private void handlePost(HttpExchange exchange) throws IOException {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> data = JsonUtil.fromJson(body);
            try {
                List<Map<String, Object>> payments = readData("payments.json");
                int nextId = nextId(payments);
                Map<String, Object> row = new HashMap<>();
                row.put("id", nextId);
                row.put("invoiceNo", data.getOrDefault("invoiceNo", ""));
                row.put("paymentDate", data.getOrDefault("paymentDate", ""));
                row.put("amount", parseDouble(data.getOrDefault("amount", "0")));
                row.put("paymentMode", data.getOrDefault("paymentMode", ""));
                row.put("status", data.getOrDefault("status", "Pending"));
                row.put("remarks", data.getOrDefault("remarks", ""));
                payments.add(row);
                writeData("payments.json", payments);
                sendJson(exchange, 201, "{\"status\": \"ok\"}");
            } catch (Exception ex) {
                ex.printStackTrace();
                sendJson(exchange, 500, "{\"error\": \"Unable to save payment\"}");
            }
        }

        private void handleDelete(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            int id = QueryUtil.getInt(query, "id", -1);
            if (id <= 0) {
                sendJson(exchange, 400, "{\"error\": \"Missing id\"}");
                return;
            }
            try {
                List<Map<String, Object>> payments = readData("payments.json");
                payments.removeIf(item -> asInt(item.get("id")) == id);
                writeData("payments.json", payments);
                sendJson(exchange, 200, "{\"status\": \"deleted\"}");
            } catch (Exception ex) {
                ex.printStackTrace();
                sendJson(exchange, 500, "{\"error\": \"Unable to delete payment\"}");
            }
        }
    }

    static class SummaryHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCors(exchange);
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"error\": \"Method not allowed\"}");
                return;
            }
            try {
                int totalClients = readData("clients.json").size();
                int totalEstimates = readData("estimates.json").size();
                int totalInvoices = readData("invoices.json").size();
                int totalPayments = readData("payments.json").size();
                Map<String, Object> summary = new HashMap<>();
                summary.put("totalClients", totalClients);
                summary.put("totalEstimates", totalEstimates);
                summary.put("totalInvoices", totalInvoices);
                summary.put("totalPayments", totalPayments);
                sendJson(exchange, 200, JsonUtil.toJson(summary));
            } catch (Exception ex) {
                ex.printStackTrace();
                sendJson(exchange, 500, "{\"error\": \"Unable to create summary\"}");
            }
        }
    }

    static class StaticHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) {
                path = "/index.html";
            }
            path = URLDecoder.decode(path, StandardCharsets.UTF_8);
            Path file = ROOT.resolve(path.substring(1)).normalize();
            if (!file.startsWith(ROOT) || !Files.exists(file) || Files.isDirectory(file)) {
                sendNotFound(exchange);
                return;
            }
            String contentType = contentType(file.toString());
            byte[] bytes = Files.readAllBytes(file);
            exchange.getResponseHeaders().add("Content-Type", contentType);
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    private static List<Map<String, Object>> readData(String fileName) throws IOException {
        Path file = DATA_DIR.resolve(fileName);
        String text = Files.readString(file, StandardCharsets.UTF_8).trim();
        if (text.isEmpty()) {
            text = "[]";
        }
        return JsonUtil.fromJsonArray(text);
    }

    private static void writeData(String fileName, List<Map<String, Object>> data) throws IOException {
        Path file = DATA_DIR.resolve(fileName);
        Files.writeString(file, JsonUtil.toJson(data), StandardCharsets.UTF_8);
    }

    private static int nextId(List<Map<String, Object>> items) {
        int max = 0;
        for (Map<String, Object> item : items) {
            max = Math.max(max, asInt(item.get("id")));
        }
        return max + 1;
    }

    private static int asInt(Object object) {
        if (object instanceof Number) {
            return ((Number) object).intValue();
        }
        try {
            return Integer.parseInt(object.toString());
        } catch (Exception e) {
            return 0;
        }
    }

    private static int parseInt(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static double parseDouble(String text) {
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private static String contentType(String fileName) {
        if (fileName.endsWith(".html")) return "text/html; charset=UTF-8";
        if (fileName.endsWith(".css")) return "text/css; charset=UTF-8";
        if (fileName.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if (fileName.endsWith(".png")) return "image/png";
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) return "image/jpeg";
        if (fileName.endsWith(".svg")) return "image/svg+xml";
        return "application/octet-stream";
    }

    private static void setCors(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }

    private static void sendNotFound(HttpExchange exchange) throws IOException {
        String response = "Not Found";
        exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=UTF-8");
        exchange.sendResponseHeaders(404, response.getBytes(StandardCharsets.UTF_8).length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes(StandardCharsets.UTF_8));
        }
    }

    private static void sendJson(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
        setCors(exchange);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}