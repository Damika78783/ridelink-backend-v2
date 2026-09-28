package lk.sliit.it3130.fare_payment_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GatewayTransactionResult {

    private boolean success;
    private String transactionId;
    private String cardLast4;
    private String message;
    private String failureReason;
}
