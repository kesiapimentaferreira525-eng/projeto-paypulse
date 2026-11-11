package paypulse.api.infrastructure.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import paypulse.api.domain.model.TransactionStatus;
import paypulse.api.infrastructure.adapters.outbound.persistence.SpringDataTransactionRepository;
import paypulse.api.infrastructure.adapters.outbound.persistence.TransactionEntity;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Configuration
@Profile("demo")
public class DemoDataInitializer {

    @Bean
    public ApplicationRunner loadDemoTransactions(SpringDataTransactionRepository repository, DataSource dataSource) {
        return args -> {
            migrateLegacyStatusColumn(dataSource);
            LocalDateTime now = LocalDateTime.now();
            List<TransactionEntity> demoTransactions = List.of(
                    transaction("ACC-1001", "1250.00", TransactionStatus.APPROVED, now.minusHours(1)),
                    transaction("ACC-1002", "89.90", TransactionStatus.APPROVED, now.minusHours(3)),
                    transaction("ACC-1003", "450.00", TransactionStatus.APPROVED, now.minusDays(1)),
                    transaction("ACC-1004", "9999.99", TransactionStatus.REJECTED, now.minusDays(1).minusHours(2)),
                    transaction("ACC-1005", "275.50", TransactionStatus.PENDING, now.minusDays(2)),
                    transaction("ACC-1006", "32.10", TransactionStatus.REJECTED, now.minusDays(3)),
                    transaction("ACC-1007", "149.90", TransactionStatus.APPROVED, now),
                    transaction("ACC-1008", "68.45", TransactionStatus.APPROVED, now),
                    transaction("ACC-1009", "820.00", TransactionStatus.REJECTED, now),
                    transaction("ACC-1010", "215.70", TransactionStatus.PENDING, now),
                    transaction("ACC-1011", "125.00", TransactionStatus.APPROVED, now.minusDays(1)),
                    transaction("ACC-1012", "340.20", TransactionStatus.APPROVED, now.minusDays(1)),
                    transaction("ACC-1013", "57.80", TransactionStatus.REJECTED, now.minusDays(1)),
                    transaction("ACC-1014", "1500.00", TransactionStatus.PENDING, now.minusDays(1)),
                    transaction("ACC-1015", "44.99", TransactionStatus.APPROVED, now.minusDays(2)),
                    transaction("ACC-1016", "780.00", TransactionStatus.REJECTED, now.minusDays(2)),
                    transaction("ACC-1017", "92.30", TransactionStatus.PENDING, now.minusDays(2)),
                    transaction("ACC-1018", "2350.00", TransactionStatus.APPROVED, now.minusDays(3)),
                    transaction("ACC-1019", "19.90", TransactionStatus.REJECTED, now.minusDays(3)),
                    transaction("ACC-1020", "460.00", TransactionStatus.APPROVED, now.minusDays(4)),
                    transaction("ACC-1021", "320.25", TransactionStatus.REJECTED, now.minusDays(4)),
                    transaction("ACC-1022", "210.00", TransactionStatus.PENDING, now.minusDays(4)),
                    transaction("ACC-1023", "72.50", TransactionStatus.APPROVED, now.minusDays(5)),
                    transaction("ACC-1024", "980.00", TransactionStatus.REJECTED, now.minusDays(5)),
                    transaction("ACC-1025", "1100.10", TransactionStatus.APPROVED, now.minusDays(6)),
                    transaction("ACC-1026", "530.00", TransactionStatus.PENDING, now.minusDays(6)),
                    transaction("ACC-1027", "20.00", TransactionStatus.REJECTED, now.minusDays(7)),
                    transaction("ACC-1028", "230.00", TransactionStatus.APPROVED, now.minusDays(8)),
                    transaction("ACC-1029", "1900.00", TransactionStatus.REJECTED, now.minusDays(9)),
                    transaction("ACC-1030", "64.90", TransactionStatus.APPROVED, now.minusDays(10)),
                    transaction("ACC-1031", "875.50", TransactionStatus.PENDING, now.minusDays(11)),
                    transaction("ACC-1032", "310.00", TransactionStatus.REJECTED, now.minusDays(12)),
                    transaction("ACC-1033", "420.00", TransactionStatus.APPROVED, now.minusDays(14)),
                    transaction("ACC-1034", "2500.00", TransactionStatus.REJECTED, now.minusDays(18)),
                    transaction("ACC-1035", "136.20", TransactionStatus.PENDING, now.minusDays(22)),
                    transaction("ACC-1036", "75.00", TransactionStatus.APPROVED, now.minusDays(30)),
                    transaction("ACC-1037", "620.00", TransactionStatus.PROCESSING, now.minusMinutes(15)),
                    transaction("ACC-1038", "189.90", TransactionStatus.PROCESSING, now.minusHours(5)),
                    transaction("ACC-1039", "2450.00", TransactionStatus.PROCESSING, now.minusDays(1)),
                    transaction("ACC-1040", "310.75", TransactionStatus.PROCESSING, now.minusDays(2))
            );
            Set<String> existingAccounts = repository.findByAccountIdIn(
                            demoTransactions.stream().map(TransactionEntity::getAccountId).toList())
                    .stream()
                    .map(TransactionEntity::getAccountId)
                    .collect(Collectors.toSet());

            repository.saveAll(demoTransactions.stream()
                    .filter(transaction -> !existingAccounts.contains(transaction.getAccountId()))
                    .toList());
        };
    }

    private static void migrateLegacyStatusColumn(DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metadata = connection.getMetaData();
            if (!"MySQL".equalsIgnoreCase(metadata.getDatabaseProductName())) {
                return;
            }

            try (ResultSet columns = metadata.getColumns(
                    connection.getCatalog(), null, "transactions", "status")) {
                if (columns.next() && columns.getString("TYPE_NAME")
                        .toUpperCase(Locale.ROOT).startsWith("ENUM")) {
                    try (Statement statement = connection.createStatement()) {
                        statement.execute(
                                "ALTER TABLE transactions MODIFY COLUMN status VARCHAR(20) NOT NULL");
                    }
                }
            }
        }
    }

    private static TransactionEntity transaction(
            String accountId, String amount, TransactionStatus status, LocalDateTime createdAt) {
        return new TransactionEntity(
                UUID.randomUUID(),
                accountId,
                new BigDecimal(amount),
                status,
                createdAt
        );
    }
}
