package paypulse.api.domain.port.outbound;

import paypulse.api.domain.model.Transaction;

public interface PaymentGatewayPort {
    boolean authorize(Transaction transaction);
}