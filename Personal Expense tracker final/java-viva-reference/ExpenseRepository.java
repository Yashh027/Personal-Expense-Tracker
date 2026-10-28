import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

// Repository class: saves and loads transactions from a local text file.
public class ExpenseRepository {
    private final Path filePath = Path.of("data", "expenses.txt");

    public ExpenseRepository() {
        try {
            Files.createDirectories(filePath.getParent());
            if (Files.notExists(filePath)) Files.writeString(filePath, "");
        } catch (IOException exception) {
            throw new RuntimeException("Could not prepare transaction storage.", exception);
        }
    }

    public synchronized List<Expense> findAll() {
        List<Expense> transactions = new ArrayList<>();
        try {
            for (String line : Files.readAllLines(filePath, StandardCharsets.UTF_8)) {
                if (line.isBlank()) continue;
                String[] p = line.split("\\t", -1);
                if (p.length != 6 && p.length != 7) continue;
                String type = p.length == 7 ? decode(p[6]) : "Expense";
                transactions.add(new Expense(decode(p[0]), decode(p[1]), Double.parseDouble(p[2]),
                    decode(p[3]), decode(p[4]), decode(p[5]), type));
            }
            return transactions;
        } catch (Exception exception) {
            throw new RuntimeException("Could not read transaction data.", exception);
        }
    }

    public synchronized void saveAll(List<Expense> transactions) {
        List<String> lines = new ArrayList<>();
        for (Expense item : transactions) {
            lines.add(encode(item.getId()) + "\t" + encode(item.getTitle()) + "\t"
                + item.getAmount() + "\t" + encode(item.getCategory()) + "\t"
                + encode(item.getDate()) + "\t" + encode(item.getPayment()) + "\t" + encode(item.getType()));
        }
        try {
            Files.write(filePath, lines, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new RuntimeException("Could not save transaction data.", exception);
        }
    }

    private String encode(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }
    private String decode(String value) {
        return new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
    }
}
