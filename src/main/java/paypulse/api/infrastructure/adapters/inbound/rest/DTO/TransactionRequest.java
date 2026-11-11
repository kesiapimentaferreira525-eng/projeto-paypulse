package paypulse.api.infrastructure.adapters.inbound.rest.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(name = "TransactionRequest", description = "Dados necessários para processar um pagamento.")
public class TransactionRequest {

    @Schema(description = "Identificador da conta", example = "ACC-2048", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "O ID da conta é obrigatório.")
    private String accountId;

    @Schema(description = "Valor do pagamento em reais", example = "125.50", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "O valor da transação é obrigatório.")
    @DecimalMin(value = "0.01", message = "O valor da transação deve ser maior que zero.")
    @Digits(integer = 17, fraction = 2,
            message = "O valor deve ter no máximo 17 dígitos inteiros e 2 casas decimais.")
    private BigDecimal amount;

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}