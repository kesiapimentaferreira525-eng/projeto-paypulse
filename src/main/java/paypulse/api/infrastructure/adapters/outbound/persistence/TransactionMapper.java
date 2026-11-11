package paypulse.api.infrastructure.adapters.outbound.persistence;

import paypulse.api.domain.model.Transaction;

public class TransactionMapper {

    // Converte de Domínio para Entidade JPA (para salvar no banco)
    public static TransactionEntity toEntity(Transaction domain) {
        return new TransactionEntity(
                domain.getId(),
                domain.getAccountId(),
                domain.getAmount(),
                domain.getStatus(),
                domain.getCreatedAt()
        );
    }

    // Converte de Entidade JPA para Domínio (para retornar ao caso de uso)
    public static Transaction toDomain(TransactionEntity entity) {
        return new Transaction(
                entity.getId(),
                entity.getAccountId(),
                entity.getAmount(),
                entity.getStatus(),
                entity.getCreatedAt()
        );
    }
}