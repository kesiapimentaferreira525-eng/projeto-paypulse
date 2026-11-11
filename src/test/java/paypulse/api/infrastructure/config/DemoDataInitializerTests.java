package paypulse.api.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import paypulse.api.domain.model.Transaction;
import paypulse.api.domain.model.TransactionStatus;
import paypulse.api.domain.port.inbound.FindTransactionsUseCase;
import paypulse.api.infrastructure.adapters.outbound.persistence.SpringDataTransactionRepository;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:paypulse-demo;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@ActiveProfiles("demo")
class DemoDataInitializerTests {

    @Autowired
    private FindTransactionsUseCase findTransactionsUseCase;

    @Autowired
    private SpringDataTransactionRepository repository;

    @Autowired
    private DataSource dataSource;

    @Test
    void loadsFictionalTransactionsThatTheTableCanDisplay() {
        List<Transaction> transactions = findTransactionsUseCase.findAll();

        assertEquals(40, transactions.size());
        assertEquals(16, transactions.stream()
                .filter(transaction -> transaction.getStatus() == TransactionStatus.APPROVED)
                .count());
        assertEquals(12, transactions.stream()
                .filter(transaction -> transaction.getStatus() == TransactionStatus.REJECTED)
                .count());
        assertEquals(8, transactions.stream()
                .filter(transaction -> transaction.getStatus() == TransactionStatus.PENDING)
                .count());
        assertEquals(4, transactions.stream()
                .filter(transaction -> transaction.getStatus() == TransactionStatus.PROCESSING)
                .count());
        assertEquals(new BigDecimal("1789.90"), transactions.stream()
                .filter(transaction -> transaction.getAccountId().matches("ACC-100[1-3]"))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    @Test
    void loadingDemoDataAgainDoesNotCreateDuplicates() throws Exception {
        new DemoDataInitializer().loadDemoTransactions(repository, dataSource)
                .run(new DefaultApplicationArguments());

        assertEquals(40, repository.count());
    }
}
