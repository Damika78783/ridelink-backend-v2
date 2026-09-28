package lk.sliit.it3130.fare_payment_service.service.gateway;

import lk.sliit.it3130.fare_payment_service.dto.GatewayTransactionResult;
import lk.sliit.it3130.fare_payment_service.model.PaymentMethod;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class SimulatedPaymentGateway implements PaymentGateway {

    private static final String SIMULATED_FAILURE_SUFFIX = "0000";

    @Override
    public GatewayTransactionResult processPayment(PaymentMethod method, BigDecimal amount, String cardToken) {
        if (method == null) {
            return GatewayTransactionResult.builder()
                    .success(false)
                    .failureReason("Payment method cannot be null")
                    .build();
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return GatewayTransactionResult.builder()
                    .success(false)
                    .failureReason("Payment amount must be greater than zero")
                    .build();
        }

        switch (method) {
            case CARD:
                return processCardPayment(cardToken);

            case CASH:
                return GatewayTransactionResult.builder()
                        .success(true)
                        .transactionId("TXN-CASH-" + generateShortId())
                        .message("Cash payment registered successfully")
                        .build();

            case WALLET:
                return GatewayTransactionResult.builder()
                        .success(true)
                        .transactionId("TXN-WALLET-" + generateShortId())
                        .message("Digital wallet payment processed successfully")
                        .build();

            default:
                return GatewayTransactionResult.builder()
                        .success(false)
                        .failureReason("Unsupported payment method: " + method)
                        .build();
        }
    }

    private GatewayTransactionResult processCardPayment(String cardToken) {
        if (cardToken == null || cardToken.trim().isEmpty()) {
            return GatewayTransactionResult.builder()
                    .success(false)
                    .failureReason("Card token is required for card payments")
                    .build();
        }

        String trimmedToken = cardToken.trim();

        // Simulated card decline rule: tokens ending with '0000' trigger a payment decline
        if (trimmedToken.endsWith(SIMULATED_FAILURE_SUFFIX)) {
            return GatewayTransactionResult.builder()
                    .success(false)
                    .cardLast4(SIMULATED_FAILURE_SUFFIX)
                    .failureReason("Card transaction declined: Simulated decline for test token ending in 0000")
                    .build();
        }

        String last4 = trimmedToken.length() >= 4
                ? trimmedToken.substring(trimmedToken.length() - 4)
                : "4242";

        return GatewayTransactionResult.builder()
                .success(true)
                .transactionId("TXN-CARD-" + generateShortId())
                .cardLast4(last4)
                .message("Card payment processed successfully")
                .build();
    }

    private String generateShortId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }
}
