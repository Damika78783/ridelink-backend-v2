package lk.sliit.it3130.fare_payment_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lk.sliit.it3130.fare_payment_service.dto.ErrorResponse;
import lk.sliit.it3130.fare_payment_service.dto.PaymentRequest;
import lk.sliit.it3130.fare_payment_service.dto.PaymentResponse;
import lk.sliit.it3130.fare_payment_service.dto.ReceiptResponse;
import lk.sliit.it3130.fare_payment_service.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payments", description = "Endpoints for processing ride payments, retrieving transactions, and generating receipts")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Operation(
            summary = "Process ride payment",
            description = "Validates completed ride status with Ride Management Service, prevents duplicate payments, and executes payment processing."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Payment processed successfully",
                    content = @Content(schema = @Schema(implementation = PaymentResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ride not completed or payment processing failed (e.g. card token ending in 0000)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Ride not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Duplicate payment - ride is already paid",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.processPayment(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Get payment by ID",
            description = "Retrieves a specific payment record by its unique database identifier."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Payment found",
                    content = @Content(schema = @Schema(implementation = PaymentResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Payment not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable Long id) {
        PaymentResponse response = paymentService.getPaymentById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get payment by Ride ID",
            description = "Retrieves the payment transaction associated with a particular ride identifier."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Payment found for ride",
                    content = @Content(schema = @Schema(implementation = PaymentResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Payment not found for given ride ID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/ride/{rideId}")
    public ResponseEntity<PaymentResponse> getPaymentByRideId(@PathVariable Long rideId) {
        PaymentResponse response = paymentService.getPaymentByRideId(rideId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get payment receipt",
            description = "Generates a detailed, itemized receipt breakdown for a given payment ID with masked card information."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Receipt generated successfully",
                    content = @Content(schema = @Schema(implementation = ReceiptResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Payment not found for receipt generation",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/{id}/receipt")
    public ResponseEntity<ReceiptResponse> getPaymentReceipt(@PathVariable Long id) {
        ReceiptResponse response = paymentService.getReceiptByPaymentId(id);
        return ResponseEntity.ok(response);
    }
}
