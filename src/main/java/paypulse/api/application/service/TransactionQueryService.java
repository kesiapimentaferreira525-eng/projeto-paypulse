package paypulse.api.application.service;

import paypulse.api.domain.model.Transaction;
import paypulse.api.domain.port.inbound.FindTransactionsUseCase;
import paypulse.api.domain.port.outbound.TransactionRepositoryPort;

import java.util.List;

public class TransactionQueryService implements FindTransactionsUseCase {

    private final TransactionRepositoryPort repositoryPort;

    public TransactionQueryService(TransactionRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    public List<Transaction> findAll() {
        return repositoryPort.findAll();
    }
}