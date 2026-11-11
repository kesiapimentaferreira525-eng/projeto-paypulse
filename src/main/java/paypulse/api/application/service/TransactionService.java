package paypulse.api.application.service;

import paypulse.api.domain.model.Transaction;
import paypulse.api.domain.model.TransactionStatus;
import paypulse.api.domain.port.inbound.ProcessTransactionUseCase;
import paypulse.api.domain.port.outbound.PaymentGatewayPort;
import paypulse.api.domain.port.outbound.TransactionRepositoryPort;

public class TransactionService implements ProcessTransactionUseCase {

    private final TransactionRepositoryPort transactionRepositoryPort;
    private final PaymentGatewayPort paymentGatewayPort;

    // Injeção de dependência via construtor (padrão limpo e ideal para testes)
    public TransactionService(TransactionRepositoryPort transactionRepositoryPort,
                              PaymentGatewayPort paymentGatewayPort) {
        this.transactionRepositoryPort = transactionRepositoryPort;
        this.paymentGatewayPort = paymentGatewayPort;
    }

    @Override
    public Transaction process(Transaction transaction) {
        if (transaction.getStatus() != TransactionStatus.PENDING) {
            throw new IllegalStateException("Somente transações pendentes podem ser processadas.");
        }

        // 1. Simula a autorização no gateway de pagamento externo
        boolean isAuthorized = paymentGatewayPort.authorize(transaction);

        // 2. Aplica a regra de negócio do domínio baseada no resultado
        if (isAuthorized) {
            transaction.approve();
        } else {
            transaction.reject();
        }

        // 3. Salva e retorna a transação atualizada através da porta de saída
        return transactionRepositoryPort.save(transaction);
    }
}