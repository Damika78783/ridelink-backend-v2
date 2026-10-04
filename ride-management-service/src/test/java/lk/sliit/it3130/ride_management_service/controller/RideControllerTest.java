package lk.sliit.it3130.ride_management_service.controller;

import lk.sliit.it3130.ride_management_service.dto.RideResponse;
import lk.sliit.it3130.ride_management_service.exception.InvalidStatusTransitionException;
import lk.sliit.it3130.ride_management_service.exception.RideNotFoundException;
import lk.sliit.it3130.ride_management_service.model.RideStatus;
import lk.sliit.it3130.ride_management_service.service.RideService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.List;

import static lk.sliit.it3130.ride_management_service.support.JwtTestTokens.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-layer tests: real security filter chain + real JWT validation + real GlobalExceptionHandler,
 * with RideService mocked so only HTTP/security/validation behaviour is under test.
 */
@SpringBootTest
@ActiveProfiles("test")
class RideControllerTest {

    @Autowired private WebApplicationContext context;
    @MockitoBean private RideService rideService;

    private MockMvc mockMvc;

    private static final String PASSENGER = "passenger@test.com";
    private static final String OTHER_PASSENGER = "other@test.com";
    private static final String DRIVER = "driver@test.com";
    private static final String ADMIN = "admin@test.com";

    private static final String VALID_BODY =
            "{\"pickupLocation\":\"Colombo Fort\",\"destinationLocation\":\"Kandy\"}";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    private RideResponse response(Long id, RideStatus status) {
        return new RideResponse(id, PASSENGER, DRIVER, "Colombo Fort", "Kandy",
                status, LocalDateTime.now(), LocalDateTime.now());
    }

    // ================= Authentication (401) =================

    @Test
    @DisplayName("401: no Authorization header")
    void noToken_returns401() throws Exception {
        mockMvc.perform(get("/api/rides/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
        verifyNoInteractions(rideService);
    }

    @Test
    @DisplayName("401: garbage token")
    void malformedToken_returns401() throws Exception {
        mockMvc.perform(get("/api/rides/1").header("Authorization", "Bearer not.a.jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("401: expired token")
    void expiredToken_returns401() throws Exception {
        mockMvc.perform(get("/api/rides/1").header("Authorization", expiredBearer(PASSENGER, "PASSENGER")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("401: token signed with a different secret")
    void wrongSignature_returns401() throws Exception {
        mockMvc.perform(get("/api/rides/1").header("Authorization", wrongSignatureBearer(PASSENGER, "PASSENGER")))
                .andExpect(status().isUnauthorized());
    }

    // ================= POST /api/rides =================

    @Test
    @DisplayName("POST /api/rides: PASSENGER gets 201 and identity comes from the JWT, not the body")
    void createRide_success_usesJwtIdentity() throws Exception {
        when(rideService.createRide(eq(PASSENGER), any())).thenReturn(response(1L, RideStatus.REQUESTED));

        String bodyWithSpoofedPassenger =
                "{\"pickupLocation\":\"Colombo Fort\",\"destinationLocation\":\"Kandy\",\"passengerEmail\":\"hacker@evil.com\"}";

        mockMvc.perform(post("/api/rides")
                        .header("Authorization", bearer(PASSENGER, "PASSENGER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyWithSpoofedPassenger))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("REQUESTED"));

        verify(rideService).createRide(eq(PASSENGER), any());
        verify(rideService, never()).createRide(eq("hacker@evil.com"), any());
    }

    @Test
    @DisplayName("POST /api/rides: DRIVER is forbidden (403)")
    void createRide_driverForbidden() throws Exception {
        mockMvc.perform(post("/api/rides")
                        .header("Authorization", bearer(DRIVER, "DRIVER"))
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isForbidden());
        verifyNoInteractions(rideService);
    }

    @Test
    @DisplayName("POST /api/rides: ADMIN is forbidden (only passengers book rides)")
    void createRide_adminForbidden() throws Exception {
        mockMvc.perform(post("/api/rides")
                        .header("Authorization", bearer(ADMIN, "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/rides: blank pickup -> 400 with validation message")
    void createRide_blankPickup_returns400() throws Exception {
        mockMvc.perform(post("/api/rides")
                        .header("Authorization", bearer(PASSENGER, "PASSENGER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pickupLocation\":\"  \",\"destinationLocation\":\"Kandy\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Pickup location is required"));
        verifyNoInteractions(rideService);
    }

    @Test
    @DisplayName("POST /api/rides: missing destination -> 400 with validation message")
    void createRide_missingDestination_returns400() throws Exception {
        mockMvc.perform(post("/api/rides")
                        .header("Authorization", bearer(PASSENGER, "PASSENGER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pickupLocation\":\"Galle\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Destination location is required"));
    }

    @Test
    @DisplayName("POST /api/rides: malformed JSON -> 400")
    void createRide_malformedJson_returns400() throws Exception {
        mockMvc.perform(post("/api/rides")
                        .header("Authorization", bearer(PASSENGER, "PASSENGER"))
                        .contentType(MediaType.APPLICATION_JSON).content("{ not json"))
                .andExpect(status().isBadRequest());
    }

    // ================= GET /api/rides/{id} =================

    @Test
    @DisplayName("GET /api/rides/{id}: any authenticated role gets 200")
    void getRide_success() throws Exception {
        when(rideService.getRide(7L)).thenReturn(response(7L, RideStatus.ASSIGNED));

        mockMvc.perform(get("/api/rides/7").header("Authorization", bearer(DRIVER, "DRIVER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.status").value("ASSIGNED"))
                .andExpect(jsonPath("$.driverEmail").value(DRIVER));
    }

    @Test
    @DisplayName("GET /api/rides/{id}: unknown id -> 404 in consistent error format")
    void getRide_notFound() throws Exception {
        when(rideService.getRide(99L)).thenThrow(new RideNotFoundException("Ride not found with id: 99"));

        mockMvc.perform(get("/api/rides/99").header("Authorization", bearer(PASSENGER, "PASSENGER")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Ride not found with id: 99"));
    }

    // ================= GET /passenger/{email}, /driver/{email} =================

    @Test
    @DisplayName("GET /passenger/{email}: passenger can list OWN rides")
    void listByPassenger_own() throws Exception {
        when(rideService.getRidesByPassenger(PASSENGER)).thenReturn(List.of(response(1L, RideStatus.REQUESTED)));

        mockMvc.perform(get("/api/rides/passenger/" + PASSENGER)
                        .header("Authorization", bearer(PASSENGER, "PASSENGER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("GET /passenger/{email}: passenger cannot list ANOTHER passenger's rides (403)")
    void listByPassenger_otherPassenger_forbidden() throws Exception {
        mockMvc.perform(get("/api/rides/passenger/" + PASSENGER)
                        .header("Authorization", bearer(OTHER_PASSENGER, "PASSENGER")))
                .andExpect(status().isForbidden());
        verifyNoInteractions(rideService);
    }

    @Test
    @DisplayName("GET /passenger/{email}: admin can list anyone's rides")
    void listByPassenger_admin() throws Exception {
        when(rideService.getRidesByPassenger(PASSENGER)).thenReturn(List.of());

        mockMvc.perform(get("/api/rides/passenger/" + PASSENGER)
                        .header("Authorization", bearer(ADMIN, "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /driver/{email}: driver can list OWN rides")
    void listByDriver_own() throws Exception {
        when(rideService.getRidesByDriver(DRIVER)).thenReturn(List.of(response(2L, RideStatus.ACCEPTED)));

        mockMvc.perform(get("/api/rides/driver/" + DRIVER)
                        .header("Authorization", bearer(DRIVER, "DRIVER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("ACCEPTED"));
    }

    @Test
    @DisplayName("GET /driver/{email}: another driver is forbidden (403)")
    void listByDriver_otherDriver_forbidden() throws Exception {
        mockMvc.perform(get("/api/rides/driver/" + DRIVER)
                        .header("Authorization", bearer("driver2@test.com", "DRIVER")))
                .andExpect(status().isForbidden());
    }

    // ================= PATCH /{id}/assign =================

    @Test
    @DisplayName("PATCH /{id}/assign: PASSENGER can trigger assignment")
    void assign_passenger_success() throws Exception {
        when(rideService.assignDriver(1L)).thenReturn(response(1L, RideStatus.ASSIGNED));

        mockMvc.perform(patch("/api/rides/1/assign").header("Authorization", bearer(PASSENGER, "PASSENGER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ASSIGNED"));
    }

    @Test
    @DisplayName("PATCH /{id}/assign: DRIVER is forbidden (403)")
    void assign_driver_forbidden() throws Exception {
        mockMvc.perform(patch("/api/rides/1/assign").header("Authorization", bearer(DRIVER, "DRIVER")))
                .andExpect(status().isForbidden());
        verifyNoInteractions(rideService);
    }

    @Test
    @DisplayName("PATCH /{id}/assign: no available driver -> 409")
    void assign_noDriver_returns409() throws Exception {
        when(rideService.assignDriver(1L))
                .thenThrow(new InvalidStatusTransitionException("No available driver found for this pickup location"));

        mockMvc.perform(patch("/api/rides/1/assign").header("Authorization", bearer(PASSENGER, "PASSENGER")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("No available driver found for this pickup location"));
    }

    // ================= PATCH /{id}/status =================

    @Test
    @DisplayName("PATCH /{id}/status: valid transition -> 200")
    void updateStatus_success() throws Exception {
        when(rideService.updateStatus(1L, RideStatus.ACCEPTED)).thenReturn(response(1L, RideStatus.ACCEPTED));

        mockMvc.perform(patch("/api/rides/1/status")
                        .header("Authorization", bearer(DRIVER, "DRIVER"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"ACCEPTED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }

    @Test
    @DisplayName("PATCH /{id}/status: invalid transition -> 409")
    void updateStatus_invalidTransition_returns409() throws Exception {
        when(rideService.updateStatus(1L, RideStatus.COMPLETED))
                .thenThrow(new InvalidStatusTransitionException("Cannot transition ride from REQUESTED to COMPLETED"));

        mockMvc.perform(patch("/api/rides/1/status")
                        .header("Authorization", bearer(DRIVER, "DRIVER"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"COMPLETED\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Invalid Status Transition"));
    }

    @Test
    @DisplayName("PATCH /{id}/status: missing status -> 400")
    void updateStatus_missingStatus_returns400() throws Exception {
        mockMvc.perform(patch("/api/rides/1/status")
                        .header("Authorization", bearer(DRIVER, "DRIVER"))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Status is required"));
    }

    @Test
    @DisplayName("PATCH /{id}/status: unknown status value -> 400")
    void updateStatus_unknownEnum_returns400() throws Exception {
        mockMvc.perform(patch("/api/rides/1/status")
                        .header("Authorization", bearer(DRIVER, "DRIVER"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"FLYING\"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(rideService);
    }

    @Test
    @DisplayName("PATCH /{id}/status: unknown ride -> 404")
    void updateStatus_rideNotFound_returns404() throws Exception {
        when(rideService.updateStatus(404L, RideStatus.CANCELLED))
                .thenThrow(new RideNotFoundException("Ride not found with id: 404"));

        mockMvc.perform(patch("/api/rides/404/status")
                        .header("Authorization", bearer(PASSENGER, "PASSENGER"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"CANCELLED\"}"))
                .andExpect(status().isNotFound());
    }
}
