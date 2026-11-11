const API_URL = "/api/v1/transactions";
const PAGE_SIZE = 8;

const state = {
  transactions: [],
  page: 1,
  search: "",
  status: "all",
  toastTimeout: null
};

const statusDetails = {
  APPROVED: { label: "Aprovada", className: "status-approved" },
  REJECTED: { label: "Recusada", className: "status-rejected" },
  PENDING: { label: "Pendente", className: "status-pending" },
  PROCESSING: { label: "Em processamento", className: "status-processing" }
};

const currency = new Intl.NumberFormat("pt-BR", {
  style: "currency",
  currency: "BRL"
});

const dateTime = new Intl.DateTimeFormat("pt-BR", {
  day: "2-digit",
  month: "short",
  year: "numeric",
  hour: "2-digit",
  minute: "2-digit"
});

const shortDate = new Intl.DateTimeFormat("pt-BR", { day: "2-digit", month: "short" });
const weekday = new Intl.DateTimeFormat("pt-BR", { weekday: "short" });
const numberFormat = new Intl.NumberFormat("pt-BR");

const elements = {
  approvedVolume: document.getElementById("approved-volume"),
  transactionCount: document.getElementById("transaction-count"),
  transactionNote: document.getElementById("transaction-note"),
  approvalRate: document.getElementById("approval-rate"),
  pendingCount: document.getElementById("pending-count"),
  processingCount: document.getElementById("processing-count"),
  approvedCount: document.getElementById("approved-count"),
  rejectedCount: document.getElementById("rejected-count"),
  legendPendingCount: document.getElementById("legend-pending-count"),
  legendProcessingCount: document.getElementById("legend-processing-count"),
  ringTotal: document.getElementById("ring-total"),
  statusRing: document.getElementById("status-ring"),
  weekVolume: document.getElementById("week-volume"),
  chart: document.getElementById("activity-chart"),
  chartLabels: document.getElementById("chart-labels"),
  chartMax: document.getElementById("chart-max"),
  chartMid: document.getElementById("chart-mid"),
  rows: document.getElementById("transaction-rows"),
  tableCount: document.getElementById("table-count"),
  paginationLabel: document.getElementById("pagination-label"),
  pageNumber: document.getElementById("page-number"),
  previousPage: document.getElementById("previous-page"),
  nextPage: document.getElementById("next-page"),
  searchInput: document.getElementById("search-input"),
  statusFilter: document.getElementById("status-filter"),
  loadAlert: document.getElementById("load-alert"),
  loadError: document.getElementById("load-error"),
  refreshButton: document.getElementById("refresh-button"),
  navCount: document.getElementById("nav-count"),
  lastUpdated: document.getElementById("last-updated"),
  todayLabel: document.getElementById("today-label"),
  dialog: document.getElementById("transaction-dialog"),
  form: document.getElementById("transaction-form"),
  submitButton: document.getElementById("submit-transaction"),
  toast: document.getElementById("toast")
};

function getStatusCount(status) {
  return state.transactions.filter((transaction) => transaction.status === status).length;
}

function getVisibleTransactions() {
  const search = state.search.trim().toLocaleLowerCase("pt-BR");

  return [...state.transactions]
    .filter((transaction) => state.status === "all" || transaction.status === state.status)
    .filter((transaction) => {
      if (!search) return true;
      return String(transaction.accountId).toLocaleLowerCase("pt-BR").includes(search)
        || String(transaction.id).toLocaleLowerCase("pt-BR").includes(search);
    })
    .sort((left, right) => new Date(right.createdAt) - new Date(left.createdAt));
}

function renderMetrics() {
  const approved = getStatusCount("APPROVED");
  const rejected = getStatusCount("REJECTED");
  const pending = getStatusCount("PENDING");
  const processing = getStatusCount("PROCESSING");
  const total = state.transactions.length;
  const approvedVolume = state.transactions
    .filter((transaction) => transaction.status === "APPROVED")
    .reduce((sum, transaction) => sum + Number(transaction.amount), 0);
  const processed = approved + rejected;
  const approvalRate = processed ? Math.round((approved / processed) * 100) : 0;

  elements.approvedVolume.textContent = currency.format(approvedVolume);
  elements.transactionCount.textContent = numberFormat.format(total);
  elements.transactionNote.textContent = total === 1 ? "Movimentação na sua conta" : "Movimentações na sua conta";
  elements.approvalRate.textContent = `${approvalRate}%`;
  elements.pendingCount.textContent = numberFormat.format(pending);
  elements.processingCount.textContent = numberFormat.format(processing);
  elements.approvedCount.textContent = numberFormat.format(approved);
  elements.rejectedCount.textContent = numberFormat.format(rejected);
  elements.legendPendingCount.textContent = numberFormat.format(pending);
  elements.legendProcessingCount.textContent = numberFormat.format(processing);
  elements.ringTotal.textContent = numberFormat.format(total);
  elements.navCount.textContent = numberFormat.format(total);

  const safeTotal = total || 1;
  const approvedEnd = (approved / safeTotal) * 100;
  const rejectedEnd = approvedEnd + (rejected / safeTotal) * 100;
  const pendingEnd = rejectedEnd + (pending / safeTotal) * 100;
  elements.statusRing.style.background = `conic-gradient(
    #10a878 0% ${approvedEnd}%,
    #df6671 ${approvedEnd}% ${rejectedEnd}%,
    #e6a543 ${rejectedEnd}% ${pendingEnd}%,
    #5488dc ${pendingEnd}% 100%
  )`;
  if (!total) {
    elements.statusRing.style.background = "conic-gradient(#e1e7ee 0% 100%)";
  }
}

function getLastSevenDays() {
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  return Array.from({ length: 7 }, (_, index) => {
    const date = new Date(today);
    date.setDate(today.getDate() - (6 - index));
    return date;
  });
}

function renderActivityChart() {
  const days = getLastSevenDays();
  const totals = days.map((day) => state.transactions
    .filter((transaction) => {
      const created = new Date(transaction.createdAt);
      return created.getFullYear() === day.getFullYear()
        && created.getMonth() === day.getMonth()
        && created.getDate() === day.getDate();
    })
    .reduce((sum, transaction) => sum + Number(transaction.amount), 0));
  const max = Math.max(...totals, 1);
  const mid = max / 2;
  const xStart = 20;
  const xStep = 110;
  const baseline = 188;
  const chartHeight = 145;
  const points = totals.map((amount, index) => ({
    x: xStart + xStep * index,
    y: baseline - (amount / max) * chartHeight
  }));
  const line = points.map((point, index) => `${index ? "L" : "M"} ${point.x} ${point.y}`).join(" ");
  const area = `${line} L ${points[points.length - 1].x} ${baseline} L ${points[0].x} ${baseline} Z`;

  elements.chart.innerHTML = `
    <defs>
      <linearGradient id="activity-fill" x1="0" x2="0" y1="0" y2="1">
        <stop offset="0%" stop-color="#10a878" stop-opacity=".2"></stop>
        <stop offset="100%" stop-color="#10a878" stop-opacity="0"></stop>
      </linearGradient>
    </defs>
    <line class="chart-grid-line" x1="0" y1="43" x2="700" y2="43"></line>
    <line class="chart-grid-line" x1="0" y1="115" x2="700" y2="115"></line>
    <line class="chart-grid-line" x1="0" y1="188" x2="700" y2="188"></line>
    <path class="chart-area" d="${area}"></path>
    <path class="chart-line" d="${line}"></path>
    ${points.map((point, index) => `<circle class="chart-point" cx="${point.x}" cy="${point.y}" r="4"><title>${shortDate.format(days[index])}: ${currency.format(totals[index])}</title></circle>`).join("")}
  `;
  elements.chartLabels.innerHTML = days.map((day) => `<span>${weekday.format(day).replace(".", "")}</span>`).join("");
  elements.weekVolume.textContent = currency.format(totals.reduce((sum, amount) => sum + amount, 0));
  elements.chartMax.textContent = currency.format(max);
  elements.chartMid.textContent = currency.format(mid);
}

function escapeHtml(value) {
  return String(value)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

function renderTable() {
  const transactions = getVisibleTransactions();
  const totalPages = Math.max(1, Math.ceil(transactions.length / PAGE_SIZE));
  state.page = Math.min(state.page, totalPages);
  const start = (state.page - 1) * PAGE_SIZE;
  const visible = transactions.slice(start, start + PAGE_SIZE);
  const end = Math.min(start + PAGE_SIZE, transactions.length);

  elements.tableCount.textContent = transactions.length === state.transactions.length
    ? `${numberFormat.format(transactions.length)} transações encontradas`
    : `${numberFormat.format(transactions.length)} de ${numberFormat.format(state.transactions.length)} transações`;
  elements.paginationLabel.textContent = transactions.length
    ? `Exibindo ${numberFormat.format(start + 1)}–${numberFormat.format(end)} de ${numberFormat.format(transactions.length)}`
    : "Nenhuma transação para exibir";
  elements.pageNumber.textContent = `${state.page} / ${totalPages}`;
  elements.previousPage.disabled = state.page <= 1;
  elements.nextPage.disabled = state.page >= totalPages;

  if (!visible.length) {
    elements.rows.innerHTML = `<tr><td class="table-empty" colspan="5"><span class="empty-icon">⌕</span><strong>Nenhum resultado encontrado</strong><br>Altere a busca ou o filtro para ver outras transações.</td></tr>`;
    return;
  }

  elements.rows.innerHTML = visible.map((transaction) => {
    const status = statusDetails[transaction.status] || { label: transaction.status, className: "" };
    const identifier = String(transaction.id);
    const date = new Date(transaction.createdAt);
    const initials = String(transaction.accountId || "??").slice(0, 2).toUpperCase();
    return `<tr>
      <td><div class="transaction-id"><span class="transaction-mark">${escapeHtml(initials)}</span><span><strong>${escapeHtml(identifier.slice(0, 8))}</strong><small>ID · ${escapeHtml(identifier.slice(-6))}</small></span></div></td>
      <td class="account-value">${escapeHtml(transaction.accountId)}</td>
      <td class="date-value">${escapeHtml(dateTime.format(date))}</td>
      <td class="amount-value">${escapeHtml(currency.format(Number(transaction.amount)))}</td>
      <td><span class="status-badge ${status.className}">${escapeHtml(status.label)}</span></td>
    </tr>`;
  }).join("");
}

function renderDashboard() {
  renderMetrics();
  renderActivityChart();
  renderTable();
  elements.lastUpdated.textContent = new Intl.DateTimeFormat("pt-BR", {
    hour: "2-digit",
    minute: "2-digit"
  }).format(new Date());
}

async function loadTransactions() {
  elements.refreshButton.disabled = true;
  elements.loadAlert.hidden = true;
  try {
    const response = await fetch(API_URL, { headers: { Accept: "application/json" } });
    if (!response.ok) throw new Error(`A API respondeu com o status ${response.status}.`);
    const transactions = await response.json();
    if (!Array.isArray(transactions)) throw new Error("A resposta da API não contém uma lista de transações.");
    state.transactions = transactions;
    state.page = 1;
    renderDashboard();
  } catch (error) {
    elements.loadError.textContent = `${error.message} Confira se a API está em execução e tente novamente.`;
    elements.loadAlert.hidden = false;
    elements.rows.innerHTML = `<tr><td class="table-empty" colspan="5">Não foi possível conectar à API.</td></tr>`;
    elements.tableCount.textContent = "Dados indisponíveis";
  } finally {
    elements.refreshButton.disabled = false;
  }
}

function showToast(message) {
  elements.toast.textContent = message;
  elements.toast.classList.add("visible");
  window.clearTimeout(state.toastTimeout);
  state.toastTimeout = window.setTimeout(() => elements.toast.classList.remove("visible"), 3600);
}

async function submitTransaction(event) {
  event.preventDefault();
  const accountId = document.getElementById("account-input").value.trim();
  const amount = document.getElementById("amount-input").value;
  elements.submitButton.disabled = true;
  elements.submitButton.textContent = "Processando…";

  try {
    const response = await fetch(API_URL, {
      method: "POST",
      headers: { "Content-Type": "application/json", Accept: "application/json" },
      body: JSON.stringify({ accountId, amount })
    });
    const result = await response.json();
    if (!response.ok) {
      const detail = result.message || result.error || "Confira os dados e tente novamente.";
      throw new Error(detail);
    }

    state.transactions = [result, ...state.transactions.filter((transaction) => transaction.id !== result.id)];
    state.search = "";
    state.status = "all";
    elements.searchInput.value = "";
    elements.statusFilter.value = "all";
    state.page = 1;
    renderDashboard();
    elements.dialog.close();
    elements.form.reset();
    showToast(`Pagamento ${statusDetails[result.status]?.label.toLocaleLowerCase("pt-BR") || "processado"} com sucesso.`);
  } catch (error) {
    showToast(error.message);
  } finally {
    elements.submitButton.disabled = false;
    elements.submitButton.textContent = "Processar pagamento";
  }
}

elements.todayLabel.textContent = new Intl.DateTimeFormat("pt-BR", {
  day: "2-digit",
  month: "long",
  year: "numeric"
}).format(new Date());

elements.refreshButton.addEventListener("click", loadTransactions);
document.getElementById("retry-button").addEventListener("click", loadTransactions);
document.getElementById("new-transaction-button").addEventListener("click", () => elements.dialog.showModal());
document.getElementById("close-dialog").addEventListener("click", () => elements.dialog.close());
document.getElementById("cancel-dialog").addEventListener("click", () => elements.dialog.close());
elements.form.addEventListener("submit", submitTransaction);
elements.searchInput.addEventListener("input", (event) => {
  state.search = event.target.value;
  state.page = 1;
  renderTable();
});
elements.statusFilter.addEventListener("change", (event) => {
  state.status = event.target.value;
  state.page = 1;
  renderTable();
});
elements.previousPage.addEventListener("click", () => {
  state.page -= 1;
  renderTable();
});
elements.nextPage.addEventListener("click", () => {
  state.page += 1;
  renderTable();
});
document.getElementById("mobile-menu").addEventListener("click", () => {
  document.getElementById("sidebar").classList.toggle("mobile-open");
});
document.getElementById("distribution-info").addEventListener("click", () => {
  showToast("A distribuição considera todas as transações carregadas no painel.");
});

loadTransactions();
