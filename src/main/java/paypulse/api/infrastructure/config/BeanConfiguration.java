package paypulse.api.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import paypulse.api.application.service.TransactionService;
import paypulse.api.application.service.TransactionQueryService;
import paypulse.api.domain.port.inbound.FindTransactionsUseCase;
import paypulse.api.domain.port.inbound.ProcessTransactionUseCase;
import paypulse.api.domain.port.outbound.PaymentGatewayPort;
import paypulse.api.domain.port.outbound.TransactionRepositoryPort;

@Configuration
public class BeanConfiguration {

    @Bean
    public ProcessTransactionUseCase processTransactionUseCase(
            TransactionRepositoryPort transactionRepositoryPort,
            PaymentGatewayPort paymentGatewayPort) {
        return new TransactionService(transactionRepositoryPort, paymentGatewayPort);
    }

    @Bean
    public FindTransactionsUseCase findTransactionsUseCase(
            TransactionRepositoryPort transactionRepositoryPort) {
        return new TransactionQueryService(transactionRepositoryPort);
    }
}