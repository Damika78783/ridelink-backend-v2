package lk.sliit.it3130.fare_payment_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lk.sliit.it3130.fare_payment_service.dto.ErrorResponse;
import lk.sliit.it3130.fare_payment_service.dto.FareEstimateRequest;
import lk.sliit.it3130.fare_payment_service.dto.FareEstimateResponse;
import lk.sliit.it3130.fare_payment_service.service.FareService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fares")
@Tag(name = "Fare Estimation", description = "Endpoints for estimating ride fares based on GPS coordinates and vehicle types")
public class FareController {

    private final FareService fareService;

    public FareController(FareService fareService) {
        this.fareService = fareService;
    }

    @Operation(
            summary = "Estimate ride fare",
            description = "Calculates estimated fare using the Haversine distance formula, vehicle multiplier, and duration estimates."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Fare estimated successfully",
                    content = @Content(schema = @Schema(implementation = FareEstimateResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request coordinates or parameters",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - JWT token missing or invalid",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping("/estimate")
    public ResponseEntity<FareEstimateResponse> estimateFare(@Valid @RequestBody FareEstimateRequest request) {
        FareEstimateResponse response = fareService.estimateFare(request);
        return ResponseEntity.ok(response);
    }
}
