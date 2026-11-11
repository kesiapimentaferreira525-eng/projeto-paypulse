package paypulse.api.domain.port.inbound;

import paypulse.api.domain.model.Transaction;
import java.util.List;

public interface FindTransactionsUseCase {
    List<Transaction> findAll();
}