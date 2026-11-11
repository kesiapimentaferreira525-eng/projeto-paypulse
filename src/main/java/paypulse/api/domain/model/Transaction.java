package paypulse.api.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class Transaction {

    private final UUID id;
    private final String accountId;
    private final BigDecimal amount;
    private TransactionStatus status;
    private final LocalDateTime createdAt;

    // Construtor para novas transações vindas da API
    public Transaction(String accountId, BigDecimal amount) {
        validateAmount(amount);
        if (accountId == null || accountId.trim().isEmpty()) {
            throw new IllegalArgumentException("O ID da conta não pode ser vazio.");
        }

        this.id = UUID.randomUUID();
        this.accountId = accountId;
        this.amount = amount;
        this.status = TransactionStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    // Construtor completo (usado para reconstituir objetos vindos do banco de dados)
    public Transaction(UUID id, String accountId, BigDecimal amount, TransactionStatus status, LocalDateTime createdAt) {
        validateAmount(amount);
        this.id = id;
        this.accountId = accountId;
        this.amount = amount;
        this.status = status;
        this.createdAt = createdAt;
    }

    // Regras de Negócio do Domínio
    public void approve() {
        ensurePending();
        this.status = TransactionStatus.APPROVED;
    }

    public void reject() {
        ensurePending();
        this.status = TransactionStatus.REJECTED;
    }

    private static void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor da transação deve ser maior que zero.");
        }

        int integerDigits = amount.precision() - amount.scale();
        if (amount.scale() > 2 || integerDigits > 17) {
            throw new IllegalArgumentException(
                    "O valor deve ter no máximo 17 dígitos inteiros e 2 casas decimais.");
        }
    }

    private void ensurePending() {
        if (status != TransactionStatus.PENDING) {
            throw new IllegalStateException("Somente transações pendentes podem mudar de estado.");
        }
    }

    // Getters (Sem Setters públicos para manter o encapsulamento e integridade do negócio)
    public UUID getId() {
        return id;
    }

    public String getAccountId() {
        return accountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
