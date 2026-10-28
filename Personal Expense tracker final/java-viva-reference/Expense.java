import java.util.UUID;

// One Transaction object represents either money spent or money received.
public class Expense {
    private String id;
    private String title;
    private double amount;
    private String category;
    private String date;
    private String payment;
    private String type;

    public Expense(String title, double amount, String category, String date, String payment, String type) {
        this(UUID.randomUUID().toString(), title, amount, category, date, payment, type);
    }

    public Expense(String id, String title, double amount, String category, String date, String payment, String type) {
        this.id = id;
        this.title = title;
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.payment = payment;
        this.type = type;
    }

    // Backward-compatible constructor for older saved records.
    public Expense(String id, String title, double amount, String category, String date, String payment) {
        this(id, title, amount, category, date, payment, "Expense");
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public double getAmount() { return amount; }
    public String getCategory() { return category; }
    public String getDate() { return date; }
    public String getPayment() { return payment; }
    public String getType() { return type; }
}
