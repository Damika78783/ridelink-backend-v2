package lk.sliit.it3130.fare_payment_service.service.gateway;

import lk.sliit.it3130.fare_payment_service.dto.GatewayTransactionResult;
import lk.sliit.it3130.fare_payment_service.model.PaymentMethod;

import java.math.BigDecimal;

public interface PaymentGateway {

    /**
     * Processes a payment transaction with the underlying payment provider.
     *
     * @param method    PaymentMethod (CARD, CASH, WALLET)
     * @param amount    Transaction amount
     * @param cardToken Token or card reference (for CARD payments)
     * @return GatewayTransactionResult detailing transaction status, transaction ID, and masked card info
     */
    GatewayTransactionResult processPayment(PaymentMethod method, BigDecimal amount, String cardToken);
}
