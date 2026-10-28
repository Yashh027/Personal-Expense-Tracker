import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Service class: contains validation, transaction operations and financial calculations.
public class ExpenseService {
    private final ExpenseRepository repository;
    public ExpenseService(ExpenseRepository repository) { this.repository = repository; }
    public List<Expense> getAllExpenses() { return repository.findAll(); }

    public Expense addExpense(String title, double amount, String category, String date, String payment, String type) {
        if (title == null || title.trim().isEmpty()) throw new IllegalArgumentException("A transaction name is required.");
        if (!Double.isFinite(amount) || amount <= 0) throw new IllegalArgumentException("Amount must be greater than zero.");
        if (category == null || category.isBlank()) throw new IllegalArgumentException("Choose a category.");
        if (date == null || !date.matches("\\d{4}-\\d{2}-\\d{2}")) throw new IllegalArgumentException("Enter a valid date.");
        try { LocalDate.parse(date); } catch (Exception e) { throw new IllegalArgumentException("Enter a real calendar date."); }
        if (payment == null || payment.isBlank()) throw new IllegalArgumentException("Choose a payment method.");
        if (!"Income".equals(type) && !"Expense".equals(type)) throw new IllegalArgumentException("Choose income or expense.");
        Expense item = new Expense(title.trim(), amount, category, date, payment, type);
        List<Expense> all = repository.findAll(); all.add(item); repository.saveAll(all);
        return item;
    }

    public boolean deleteExpense(String id) {
        List<Expense> all = repository.findAll();
        boolean removed = all.removeIf(item -> item.getId().equals(id));
        if (removed) repository.saveAll(all);
        return removed;
    }

    public Map<String, Object> getSummary() {
        List<Expense> all = repository.findAll();
        double expenses = 0, income = 0;
        Map<String, Double> categories = new LinkedHashMap<>();
        for (Expense item : all) {
            if ("Income".equals(item.getType())) income += item.getAmount();
            else { expenses += item.getAmount(); categories.merge(item.getCategory(), item.getAmount(), Double::sum); }
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", expenses); result.put("income", income); result.put("balance", income - expenses);
        result.put("count", all.size()); result.put("categories", categories);
        return result;
    }
}
