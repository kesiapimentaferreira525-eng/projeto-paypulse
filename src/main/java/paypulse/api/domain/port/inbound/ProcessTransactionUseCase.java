package paypulse.api.domain.port.inbound;

import paypulse.api.domain.model.Transaction;

public interface ProcessTransactionUseCase {
    Transaction process(Transaction transaction);
}