package lk.sliit.it3130.fare_payment_service.controller;

import lk.sliit.it3130.fare_payment_service.dto.PaymentRequest;
import lk.sliit.it3130.fare_payment_service.dto.PaymentResponse;
import lk.sliit.it3130.fare_payment_service.dto.ReceiptResponse;
import lk.sliit.it3130.fare_payment_service.model.PaymentMethod;
import lk.sliit.it3130.fare_payment_service.model.PaymentStatus;
import lk.sliit.it3130.fare_payment_service.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

    private PaymentResponse mockPaymentResponse;

    @BeforeEach
    void setUp() {
        mockPaymentResponse = PaymentResponse.builder()
                .id(1L)
                .rideId(101L)
                .passengerId(201L)
                .amount(BigDecimal.valueOf(1260.00))
                .paymentMethod(PaymentMethod.CARD)
                .status(PaymentStatus.COMPLETED)
                .transactionId("TXN-CARD-123456")
                .cardLast4("4242")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should return 201 CREATED on processPayment")
    void testProcessPaymentEndpoint() {
        PaymentRequest request = PaymentRequest.builder()
                .rideId(101L)
                .passengerId(201L)
                .paymentMethod(PaymentMethod.CARD)
                .cardToken("tok_visa_4242")
                .build();

        when(paymentService.processPayment(any(PaymentRequest.class))).thenReturn(mockPaymentResponse);

        ResponseEntity<PaymentResponse> responseEntity = paymentController.processPayment(request);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertEquals(1L, responseEntity.getBody().getId());
        verify(paymentService).processPayment(request);
    }

    @Test
    @DisplayName("Should return 200 OK on getPaymentById")
    void testGetPaymentByIdEndpoint() {
        when(paymentService.getPaymentById(1L)).thenReturn(mockPaymentResponse);

        ResponseEntity<PaymentResponse> responseEntity = paymentController.getPaymentById(1L);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(1L, responseEntity.getBody().getId());
        verify(paymentService).getPaymentById(1L);
    }

    @Test
    @DisplayName("Should return 200 OK on getPaymentByRideId")
    void testGetPaymentByRideIdEndpoint() {
        when(paymentService.getPaymentByRideId(101L)).thenReturn(mockPaymentResponse);

        ResponseEntity<PaymentResponse> responseEntity = paymentController.getPaymentByRideId(101L);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(101L, responseEntity.getBody().getRideId());
        verify(paymentService).getPaymentByRideId(101L);
    }

    @Test
    @DisplayName("Should return 200 OK on getPaymentReceipt")
    void testGetPaymentReceiptEndpoint() {
        ReceiptResponse mockReceipt = ReceiptResponse.builder()
                .receiptNumber("REC-1")
                .transactionId("TXN-CARD-123456")
                .totalAmount(BigDecimal.valueOf(1260.00))
                .currency("LKR")
                .build();

        when(paymentService.getReceiptByPaymentId(1L)).thenReturn(mockReceipt);

        ResponseEntity<ReceiptResponse> responseEntity = paymentController.getPaymentReceipt(1L);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("REC-1", responseEntity.getBody().getReceiptNumber());
        verify(paymentService).getReceiptByPaymentId(1L);
    }
}
