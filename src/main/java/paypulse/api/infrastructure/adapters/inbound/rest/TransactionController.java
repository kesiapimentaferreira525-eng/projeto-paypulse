package paypulse.api.infrastructure.adapters.inbound.rest;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import paypulse.api.domain.model.Transaction;
import paypulse.api.domain.port.inbound.FindTransactionsUseCase;
import paypulse.api.domain.port.inbound.ProcessTransactionUseCase;
import paypulse.api.infrastructure.adapters.inbound.rest.dto.TransactionRequest;
import paypulse.api.infrastructure.adapters.inbound.rest.dto.TransactionResponse;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@CrossOrigin(origins = "*") // Permite requisições do Angular local
public class TransactionController {

    private final ProcessTransactionUseCase processTransactionUseCase;
    private final FindTransactionsUseCase findTransactionsUseCase;

    public TransactionController(ProcessTransactionUseCase processTransactionUseCase,
                                  FindTransactionsUseCase findTransactionsUseCase) {
        this.processTransactionUseCase = processTransactionUseCase;
        this.findTransactionsUseCase = findTransactionsUseCase;
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> processTransaction(@Valid @RequestBody TransactionRequest request) {
        // 1. Cria o objeto de domínio (as validações do construtor rodam aqui)
        Transaction transaction = new Transaction(request.getAccountId(), request.getAmount());

        // 2. Executa o caso de uso através da porta de entrada
        Transaction processedTransaction = processTransactionUseCase.process(transaction);

        // 3. Converte para DTO de resposta e retorna HTTP 201 Created
        TransactionResponse response = TransactionResponse.fromDomain(processedTransaction);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getAllTransactions() {
        List<TransactionResponse> responses = findTransactionsUseCase.findAll()
                .stream()
                .map(TransactionResponse::fromDomain)
                .toList();
        return ResponseEntity.ok(responses);
    }
}
