package paypulse.api.infrastructure.adapters.inbound.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import paypulse.api.domain.model.Transaction;
import paypulse.api.domain.model.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(name = "TransactionResponse", description = "Transação retornada pela API.")
public class TransactionResponse {
    @Schema(description = "Identificador único da transação", example = "2f1c96e1-1f54-4fd8-86ab-b83d921f2173")
    private UUID id;
    @Schema(description = "Identificador da conta", example = "ACC-2048")
    private String accountId;
    @Schema(description = "Valor do pagamento em reais", example = "125.50")
    private BigDecimal amount;
    @Schema(description = "Status da transação")
    private TransactionStatus status;
    @Schema(description = "Data e hora de criação", example = "2026-11-11T17:00:00")
    private LocalDateTime createdAt;

    public TransactionResponse(UUID id, String accountId, BigDecimal amount,
                               TransactionStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.accountId = accountId;
        this.amount = amount;
        this.status = status;
        this.createdAt = createdAt;
    }

    // Método estático para converter do Domínio para o DTO de resposta da API
    public static TransactionResponse fromDomain(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getAccountId(),
                transaction.getAmount(),
                transaction.getStatus(),
                transaction.getCreatedAt()
        );
    }

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