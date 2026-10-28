const form = document.getElementById("transactionForm");
const rows = document.getElementById("transactionRows");
const STORAGE_KEY = "moneta_transactions_v1";
let transactions = readTransactions();

// Read saved transactions from this browser. No server or API key is required.
function readTransactions() {
  try {
    const saved = localStorage.getItem(STORAGE_KEY);
    const parsed = saved ? JSON.parse(saved) : [];
    return Array.isArray(parsed) ? parsed : [];
  } catch (error) {
    console.error("Could not read saved transactions:", error);
    return [];
  }
}

// Save the complete transaction list in this browser.
function saveTransactions() {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(transactions));
}
const money = value => new Intl.NumberFormat("en-IN", {style:"currency", currency:"INR", maximumFractionDigits:2}).format(Number(value || 0));
const icons = {Food:"☕",Travel:"↗",Shopping:"◇",Education:"▤",Bills:"⌂",Health:"✚",Entertainment:"♫",Salary:"₹",Freelance:"✦",Gift:"♧",Other:"•"};
const today = new Date();
const dateInput = document.getElementById("date");
dateInput.value = localDate(today);
document.getElementById("todayLabel").textContent = today.toLocaleDateString("en-IN", {weekday:"short", day:"numeric", month:"short", year:"numeric"});
const yearSelect = document.getElementById("trendYear");
for (let y = today.getFullYear() - 3; y <= today.getFullYear() + 1; y++) yearSelect.add(new Option(y, y, y === today.getFullYear(), y === today.getFullYear()));

function localDate(d) { return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,"0")}-${String(d.getDate()).padStart(2,"0")}`; }
function safe(value) { return String(value).replace(/[&<>"']/g, c => ({"&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#039;"}[c])); }
function loadTransactions() {
  transactions = readTransactions();
  renderAll();
}
function totals(list) {
  return list.reduce((t, item) => { if (item.type === "Income") t.income += Number(item.amount); else t.expenses += Number(item.amount); return t; }, {income:0, expenses:0});
}
function renderAll() {
  const all = totals(transactions);
  document.getElementById("balanceAmount").textContent = money(all.income - all.expenses);
  document.getElementById("incomeAmount").textContent = money(all.income);
  document.getElementById("expenseAmount").textContent = money(all.expenses);
  document.getElementById("transactionCount").textContent = transactions.length;
  document.getElementById("heroIncome").textContent = money(all.income);
  document.getElementById("heroExpense").textContent = money(all.expenses);
  const categoryTotals = {};
  transactions.filter(t => t.type !== "Income").forEach(t => categoryTotals[t.category] = (categoryTotals[t.category] || 0) + Number(t.amount));
  const ranked = Object.entries(categoryTotals).sort((a,b) => b[1]-a[1]);
  document.getElementById("topCategory").textContent = ranked[0]?.[0] || "—";
  renderCategories(ranked, all.expenses);
  renderChart(); renderBudget(all.expenses); renderTransactions();
}
function renderCategories(ranked, total) {
  const host = document.getElementById("categoryBreakdown");
  host.innerHTML = ranked.length ? ranked.map(([name, amount], i) => `<div class="category-row"><div class="category-icon-box icon-${i%5}">${icons[name] || "•"}</div><div class="category-details"><div class="category-line"><span>${safe(name)}</span><strong>${money(amount)}</strong></div><div class="category-track"><i style="width:${total ? amount/total*100 : 0}%"></i></div></div><small>${total ? Math.round(amount/total*100) : 0}%</small></div>`).join("") : '<div class="empty-state">No spending data yet. Add an expense to see your categories.</div>';
  document.getElementById("breakdownTotal").textContent = money(total);
}
function renderChart() {
  const year = Number(yearSelect.value || today.getFullYear());
  const months = [];
  const endMonth = year === today.getFullYear() ? today.getMonth() : 11;
  for (let offset = 5; offset >= 0; offset--) { const d = new Date(year, endMonth-offset, 1); months.push({key:`${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,"0")}`, label:d.toLocaleDateString("en-IN",{month:"short"}), income:0, expense:0}); }
  transactions.forEach(t => { const m = months.find(x => x.key === t.date.slice(0,7)); if (m) m[t.type === "Income" ? "income" : "expense"] += Number(t.amount); });
  const max = Math.max(1, ...months.flatMap(m => [m.income,m.expense]));
  const host = document.getElementById("monthlyChart");
  host.innerHTML = months.map(m => `<div class="chart-column"><div class="bar-pair"><div class="bar income-bar" style="height:${Math.max(m.income ? 3 : 0,m.income/max*100)}%" title="Income: ${money(m.income)}"></div><div class="bar expense-bar" style="height:${Math.max(m.expense ? 3 : 0,m.expense/max*100)}%" title="Expenses: ${money(m.expense)}"></div></div><span>${m.label}</span></div>`).join("");
  const active = months.filter(m => m.income || m.expense).length;
  document.getElementById("chartInsight").textContent = active ? `${active} active month${active===1?"":"s"} shown` : "No activity in this period";
}
function renderBudget(expenseTotal) {
  const month = localDate(today).slice(0,7);
  const spent = transactions.filter(t => t.type !== "Income" && t.date.startsWith(month)).reduce((s,t)=>s+Number(t.amount),0);
  const budget = Number(localStorage.getItem("moneta_monthly_budget") || 0);
  document.getElementById("budgetAmount").value = budget || "";
  document.getElementById("budgetSpent").textContent = money(spent);
  const remaining = budget - spent;
  document.getElementById("budgetRemaining").textContent = budget ? money(remaining) : "—";
  const percent = budget ? Math.min(100, spent/budget*100) : 0;
  document.getElementById("budgetProgress").style.width = `${percent}%`;
  document.getElementById("budgetProgress").classList.toggle("over-budget", budget > 0 && spent > budget);
  document.getElementById("budgetMessage").textContent = !budget ? "Set a monthly budget to start tracking your progress." : spent > budget ? `You are ${money(spent-budget)} over your monthly limit.` : `${Math.round(percent)}% of your monthly budget used. ${money(remaining)} left to spend.`;
}
function filteredTransactions() {
  const search = document.getElementById("searchInput").value.trim().toLowerCase();
  const type = document.getElementById("typeFilter").value;
  const category = document.getElementById("categoryFilter").value;
  const month = document.getElementById("monthFilter").value;
  return transactions.filter(t => (!search || `${t.title} ${t.category} ${t.payment}`.toLowerCase().includes(search)) && (type === "All" || t.type === type) && (category === "All" || t.category === category) && (!month || t.date.startsWith(month))).sort((a,b)=>b.date.localeCompare(a.date));
}
function renderTransactions() {
  const visible = filteredTransactions();
  rows.innerHTML = visible.length ? visible.map(t => `<tr><td><div class="transaction-title"><span class="transaction-symbol ${t.type === "Income" ? "symbol-income" : "symbol-expense"}">${icons[t.category] || "•"}</span><span>${safe(t.title)}</span></div></td><td><span class="type-pill ${t.type === "Income" ? "income-pill" : "expense-pill"}">${safe(t.type)}</span></td><td><span class="category-pill">${safe(t.category)}</span></td><td>${formatDate(t.date)}</td><td>${safe(t.payment)}</td><td class="amount-cell ${t.type === "Income" ? "income-text" : "expense-text"}">${t.type === "Income" ? "+" : "−"}${money(t.amount)}</td><td><button class="delete-button" data-id="${safe(t.id)}" aria-label="Delete transaction" title="Delete transaction">×</button></td></tr>`).join("") : `<tr><td colspan="7" class="empty-row">${transactions.length ? "No transactions match your filters." : "No transactions yet. Add your first entry above."}</td></tr>`;
  document.getElementById("showingCount").textContent = `Showing ${visible.length} of ${transactions.length} transactions`;
}
function formatDate(value) { return new Date(`${value}T00:00:00`).toLocaleDateString("en-IN",{day:"2-digit",month:"short",year:"numeric"}); }
function toast(message) { const el=document.getElementById("toast"); el.textContent=message; el.classList.add("show"); setTimeout(()=>el.classList.remove("show"),2600); }
function updateTypeUI() { const type=form.querySelector('input[name="type"]:checked').value; document.getElementById("expenseTypeLabel").classList.toggle("selected",type==="Expense"); document.getElementById("incomeTypeLabel").classList.toggle("selected",type==="Income"); document.getElementById("submitButton").innerHTML=`Save ${type.toLowerCase()} <span>→</span>`; }
form.querySelectorAll('input[name="type"]').forEach(input=>input.addEventListener("change",updateTypeUI));
form.addEventListener("submit", async event => {
  event.preventDefault();
  const type=form.querySelector('input[name="type"]:checked').value;
  const data={title:document.getElementById("title").value.trim(),amount:document.getElementById("amount").value,category:document.getElementById("category").value,date:dateInput.value,payment:document.getElementById("payment").value,type};
  const msg=document.getElementById("formMessage");
  if (!data.title || !Number.isFinite(Number(data.amount)) || Number(data.amount)<=0 || !data.date) { msg.textContent="Enter a name, a positive amount and a valid date."; return; }
  try {
    const transaction = {
      id: (window.crypto && crypto.randomUUID) ? crypto.randomUUID() : `${Date.now()}-${Math.random().toString(16).slice(2)}`,
      ...data,
      amount: Number(data.amount)
    };
    transactions.unshift(transaction);
    saveTransactions();
    form.reset();
    dateInput.value = localDate(today);
    updateTypeUI();
    msg.textContent = `${type} saved successfully on this device.`;
    loadTransactions();
    toast(`${type} added successfully`);
  } catch (error) {
    msg.textContent = "Could not save this transaction in your browser.";
    console.error(error);
  }
});
rows.addEventListener("click", event => {
  const button = event.target.closest(".delete-button");
  if (!button) return;
  if (!confirm("Permanently delete this transaction?")) return;

  transactions = transactions.filter(transaction => String(transaction.id) !== String(button.dataset.id));
  saveTransactions();
  loadTransactions();
  toast("Transaction deleted");
});
["searchInput","typeFilter","categoryFilter","monthFilter"].forEach(id=>document.getElementById(id).addEventListener(id==="searchInput"?"input":"change",renderTransactions));
yearSelect.addEventListener("change",renderChart);
document.getElementById("saveBudget").addEventListener("click",()=>{const value=Number(document.getElementById("budgetAmount").value);if(!Number.isFinite(value)||value<=0){toast("Enter a budget greater than zero");return;}localStorage.setItem("moneta_monthly_budget",String(value));renderBudget(totals(transactions).expenses);toast("Monthly budget saved");});
document.getElementById("exportButton").addEventListener("click",()=>{const data=filteredTransactions();if(!data.length){toast("No transactions to export");return;}const header=["Title","Type","Amount (INR)","Category","Date","Payment method"];const lines=[header,...data.map(t=>[t.title,t.type,t.amount,t.category,t.date,t.payment])].map(row=>row.map(v=>`"${String(v).replace(/"/g,'""')}"`).join(","));const blob=new Blob(["\uFEFF"+lines.join("\r\n")],{type:"text/csv;charset=utf-8;"});const url=URL.createObjectURL(blob);const a=document.createElement("a");a.href=url;a.download="moneta-transactions.csv";a.click();URL.revokeObjectURL(url);toast("CSV report exported");});
loadTransactions();
