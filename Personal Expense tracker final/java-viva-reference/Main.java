import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;

// Application entry point: creates objects and starts the built-in Java HTTP server.
public class Main {
    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));

        ExpenseRepository repository = new ExpenseRepository();
        ExpenseService service = new ExpenseService(repository);
        ExpenseHandler handler = new ExpenseHandler(service);

        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
        server.createContext("/api/", handler);
        server.start();

        System.out.println("Personal Expense Tracker API running on port " + port);
    }
}
