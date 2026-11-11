package paypulse.api.infrastructure.adapters.outbound.gateway;

import org.springframework.stereotype.Component;
import paypulse.api.domain.model.Transaction;
import paypulse.api.domain.port.outbound.PaymentGatewayPort;

import java.math.BigDecimal;

@Component
public class PaymentGatewayAdapter implements PaymentGatewayPort {

    @Override
    public boolean authorize(Transaction transaction) {
        // Regra simulada: Rejeita se o valor for maior que 10.000 (ex: análise de risco)
        // Caso contrário, aprova com sucesso.
        BigDecimal riskLimit = new BigDecimal("10000.00");
        return transaction.getAmount().compareTo(riskLimit) <= 0;
    }
}