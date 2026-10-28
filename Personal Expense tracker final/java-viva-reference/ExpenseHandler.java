import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Receives browser HTTP requests and calls the appropriate service method.
public class ExpenseHandler implements HttpHandler {
    private final ExpenseService service;

    public ExpenseHandler(ExpenseService service) {
        this.service = service;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        if ("OPTIONS".equalsIgnoreCase(method)) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return;
        }

        try {
            if ("/api/health".equals(path) && "GET".equalsIgnoreCase(method)) {
                sendJson(exchange, 200, "{\"status\":\"ok\",\"message\":\"Java backend is running\"}");
            } else if ("/api/expenses".equals(path) && "GET".equalsIgnoreCase(method)) {
                sendExpenses(exchange, service.getAllExpenses());
            } else if ("/api/expenses".equals(path) && "POST".equalsIgnoreCase(method)) {
                Map<String, String> form = parseForm(
                    new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
                Expense created = service.addExpense(form.get("title"), parseAmount(form.get("amount")),
                    form.get("category"), form.get("date"), form.get("payment"),
                    form.getOrDefault("type", "Expense"));
                sendJson(exchange, 201, expenseToJson(created));
            } else if ("/api/expenses".equals(path) && "DELETE".equalsIgnoreCase(method)) {
                String id = queryValue(exchange.getRequestURI().getRawQuery(), "id");
                if (id == null || !service.deleteExpense(id)) {
                    sendJson(exchange, 404, "{\"error\":\"Expense not found.\"}");
                } else {
                    exchange.sendResponseHeaders(204, -1);
                    exchange.close();
                }
            } else if ("/api/summary".equals(path) && "GET".equalsIgnoreCase(method)) {
                sendJson(exchange, 200, summaryToJson(service.getSummary()));
            } else {
                sendJson(exchange, 404, "{\"error\":\"Endpoint not found.\"}");
            }
        } catch (IllegalArgumentException exception) {
            sendJson(exchange, 400, "{\"error\":" + quote(exception.getMessage()) + "}");
        } catch (Exception exception) {
            sendJson(exchange, 500, "{\"error\":\"Server error. Check the backend terminal.\"}");
        }
    }

    private double parseAmount(String value) {
        try { return Double.parseDouble(value); }
        catch (Exception exception) { return Double.NaN; }
    }

    private Map<String, String> parseForm(String body) {
        Map<String, String> values = new LinkedHashMap<>();
        if (body.isEmpty()) return values;
        for (String pair : body.split("&")) {
            String[] parts = pair.split("=", 2);
            String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
            String value = parts.length > 1 ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8) : "";
            values.put(key, value);
        }
        return values;
    }

    private String queryValue(String query, String key) {
        if (query == null) return null;
        for (String pair : query.split("&")) {
            String[] parts = pair.split("=", 2);
            if (parts.length == 2 && parts[0].equals(key))
                return URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
        }
        return null;
    }

    private void sendExpenses(HttpExchange exchange, List<Expense> expenses) throws IOException {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < expenses.size(); i++) {
            if (i > 0) json.append(",");
            json.append(expenseToJson(expenses.get(i)));
        }
        json.append("]");
        sendJson(exchange, 200, json.toString());
    }

    private String expenseToJson(Expense expense) {
        return "{"
            + "\"id\":" + quote(expense.getId()) + ","
            + "\"title\":" + quote(expense.getTitle()) + ","
            + "\"amount\":" + expense.getAmount() + ","
            + "\"category\":" + quote(expense.getCategory()) + ","
            + "\"date\":" + quote(expense.getDate()) + ","
            + "\"payment\":" + quote(expense.getPayment()) + ","
            + "\"type\":" + quote(expense.getType())
            + "}";
    }

    private String summaryToJson(Map<String, Object> summary) {
        @SuppressWarnings("unchecked")
        Map<String, Double> categories = (Map<String, Double>) summary.get("categories");
        StringBuilder categoryJson = new StringBuilder("{");
        int index = 0;
        for (Map.Entry<String, Double> entry : categories.entrySet()) {
            if (index++ > 0) categoryJson.append(",");
            categoryJson.append(quote(entry.getKey())).append(":").append(entry.getValue());
        }
        categoryJson.append("}");
        return "{\"total\":" + summary.get("total")
            + ",\"income\":" + summary.get("income")
            + ",\"balance\":" + summary.get("balance")
            + ",\"count\":" + summary.get("count")
            + ",\"categories\":" + categoryJson + "}";
    }

    // Adds quotes and escapes special characters so the result is valid JSON.
    private String quote(String value) {
        if (value == null) return "null";
        StringBuilder result = new StringBuilder("\"");
        for (char ch : value.toCharArray()) {
            switch (ch) {
                case '"' -> result.append("\\\"");
                case '\\' -> result.append("\\\\");
                case '\n' -> result.append("\\n");
                case '\r' -> result.append("\\r");
                case '\t' -> result.append("\\t");
                default -> {
                    if (ch < 32) result.append(String.format("\\u%04x", (int) ch));
                    else result.append(ch);
                }
            }
        }
        return result.append("\"").toString();
    }

    private void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }

    private void sendJson(HttpExchange exchange, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }
}
