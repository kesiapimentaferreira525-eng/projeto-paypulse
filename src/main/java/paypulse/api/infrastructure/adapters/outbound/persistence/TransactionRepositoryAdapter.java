package paypulse.api.infrastructure.adapters.outbound.persistence;

import org.springframework.stereotype.Component;
import paypulse.api.domain.model.Transaction;
import paypulse.api.domain.port.outbound.TransactionRepositoryPort;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class TransactionRepositoryAdapter implements TransactionRepositoryPort {

    private final SpringDataTransactionRepository repository;

    public TransactionRepositoryAdapter(SpringDataTransactionRepository repository) {
        this.repository = repository;
    }

    @Override
    public Transaction save(Transaction transaction) {
        TransactionEntity entity = TransactionMapper.toEntity(transaction);
        TransactionEntity savedEntity = repository.save(entity);
        return TransactionMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Transaction> findById(UUID id) {
        return repository.findById(id)
                .map(TransactionMapper::toDomain);
    }

    @Override
    public List<Transaction> findAll() {
        return repository.findAll()
                .stream()
                .map(TransactionMapper::toDomain)
                .toList();
    }
}