package com.ridelink.drivervehicle.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standardized error response payload")
public class ErrorResponse {

    @Schema(description = "Timestamp when error occurred")
    private LocalDateTime timestamp;

    @Schema(description = "HTTP status code", example = "404")
    private int status;

    @Schema(description = "HTTP error title", example = "Not Found")
    private String error;

    @Schema(description = "Detailed error message", example = "Driver not found with id: 66f42a1b9c3e4a2d8f1e5a7b")
    private String message;

    @Schema(description = "Target API request URI path", example = "/api/drivers/66f42a1b9c3e4a2d8f1e5a7b")
    private String path;

    @Schema(description = "Field-level validation error details")
    private Map<String, String> validationErrors;
}
