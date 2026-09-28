package lk.sliit.it3130.fare_payment_service.service.gateway;

import lk.sliit.it3130.fare_payment_service.dto.GatewayTransactionResult;
import lk.sliit.it3130.fare_payment_service.model.PaymentMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class SimulatedPaymentGatewayTest {

    private SimulatedPaymentGateway gateway;

    @BeforeEach
    void setUp() {
        gateway = new SimulatedPaymentGateway();
    }

    @Test
    @DisplayName("Should successfully process valid card payment")
    void testProcessCardPaymentSuccess() {
        GatewayTransactionResult result = gateway.processPayment(
                PaymentMethod.CARD,
                BigDecimal.valueOf(1250.00),
                "tok_visa_4242"
        );

        assertTrue(result.isSuccess());
        assertNotNull(result.getTransactionId());
        assertTrue(result.getTransactionId().startsWith("TXN-CARD-"));
        assertEquals("4242", result.getCardLast4());
        assertNull(result.getFailureReason());
    }

    @Test
    @DisplayName("Should reject card payment when token ends in 0000 (Simulated decline rule)")
    void testProcessCardPaymentFailureWith0000() {
        GatewayTransactionResult result = gateway.processPayment(
                PaymentMethod.CARD,
                BigDecimal.valueOf(1250.00),
                "tok_declined_0000"
        );

        assertFalse(result.isSuccess());
        assertEquals("0000", result.getCardLast4());
        assertNotNull(result.getFailureReason());
        assertTrue(result.getFailureReason().contains("0000"));
    }

    @Test
    @DisplayName("Should fail when card token is empty or null for CARD method")
    void testProcessCardPaymentMissingToken() {
        GatewayTransactionResult result = gateway.processPayment(
                PaymentMethod.CARD,
                BigDecimal.valueOf(500.00),
                ""
        );

        assertFalse(result.isSuccess());
        assertNotNull(result.getFailureReason());
    }

    @Test
    @DisplayName("Should successfully process CASH payment")
    void testProcessCashPaymentSuccess() {
        GatewayTransactionResult result = gateway.processPayment(
                PaymentMethod.CASH,
                BigDecimal.valueOf(750.00),
                null
        );

        assertTrue(result.isSuccess());
        assertTrue(result.getTransactionId().startsWith("TXN-CASH-"));
        assertNull(result.getCardLast4());
    }

    @Test
    @DisplayName("Should successfully process WALLET payment")
    void testProcessWalletPaymentSuccess() {
        GatewayTransactionResult result = gateway.processPayment(
                PaymentMethod.WALLET,
                BigDecimal.valueOf(300.00),
                null
        );

        assertTrue(result.isSuccess());
        assertTrue(result.getTransactionId().startsWith("TXN-WALLET-"));
    }
}
