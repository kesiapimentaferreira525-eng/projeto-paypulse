package paypulse.api.domain.model;

import paypulse.api.application.service.TransactionService;
import paypulse.api.domain.port.outbound.PaymentGatewayPort;
import paypulse.api.domain.port.outbound.TransactionRepositoryPort;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransactionTests {

    @Test
    void rejectsAmountsThatExceedDatabasePrecision() {
        assertThrows(IllegalArgumentException.class,
                () -> new Transaction("account-1", new BigDecimal("0.001")));
        assertThrows(IllegalArgumentException.class,
                () -> new Transaction("account-1", new BigDecimal("100000000000000000.00")));
    }

    @Test
    void doesNotAllowChangingATerminalStatus() {
        Transaction transaction = new Transaction("account-1", new BigDecimal("10.00"));
        transaction.approve();

        assertThrows(IllegalStateException.class, transaction::reject);
    }

    @Test
    void doesNotAuthorizeATransactionThatIsNotPending() {
        AtomicInteger authorizationCalls = new AtomicInteger();
        PaymentGatewayPort gateway = transaction -> {
            authorizationCalls.incrementAndGet();
            return true;
        };
        TransactionRepositoryPort repository = new TransactionRepositoryPort() {
            @Override
            public Transaction save(Transaction transaction) {
                return transaction;
            }

            @Override
            public Optional<Transaction> findById(UUID id) {
                return Optional.empty();
            }

            @Override
            public List<Transaction> findAll() {
                return List.of();
            }
        };
        TransactionService service = new TransactionService(repository, gateway);
        Transaction transaction = new Transaction("account-1", new BigDecimal("10.00"));
        transaction.reject();

        assertThrows(IllegalStateException.class, () -> service.process(transaction));
        assertEquals(0, authorizationCalls.get());
    }
}
