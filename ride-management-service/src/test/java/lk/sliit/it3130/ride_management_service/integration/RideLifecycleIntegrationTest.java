package lk.sliit.it3130.ride_management_service.integration;

import lk.sliit.it3130.ride_management_service.model.Ride;
import lk.sliit.it3130.ride_management_service.model.RideStatus;
import lk.sliit.it3130.ride_management_service.repository.RideRepository;
import lk.sliit.it3130.ride_management_service.service.DriverLookupService;
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static lk.sliit.it3130.ride_management_service.support.JwtTestTokens.bearer;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end tests inside the service: HTTP -> security -> controller -> service -> JPA (H2).
 * Only the outbound Driver & Vehicle lookup is mocked.
 * Covers assignment workflows 4 & 5 and negative scenario 7 from the brief.
 */
@SpringBootTest
@ActiveProfiles("test")
class RideLifecycleIntegrationTest {

    private static final String PASSENGER = "alice.passenger@test.com";
    private static final String DRIVER = "bob.driver@test.com";

    @Autowired private WebApplicationContext context;
    @Autowired private RideRepository rideRepository;
    @MockitoBean private DriverLookupService driverLookupService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        rideRepository.deleteAll();
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    // ---------- helpers ----------

    private long createRide(String passengerEmail) throws Exception {
        String body = mockMvc.perform(post("/api/rides")
                        .header("Authorization", bearer(passengerEmail, "PASSENGER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pickupLocation\":\"Colombo Fort\",\"destinationLocation\":\"Kandy\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("REQUESTED"))
                .andReturn().getResponse().getContentAsString();
        Matcher m = Pattern.compile("\"id\"\\s*:\\s*(\\d+)").matcher(body);
        assertTrue(m.find(), "response should contain ride id: " + body);
        return Long.parseLong(m.group(1));
    }

    private void assign(long id, String expectedStatus) throws Exception {
        mockMvc.perform(patch("/api/rides/" + id + "/assign")
                        .header("Authorization", bearer(PASSENGER, "PASSENGER")))
                .andExpect(status().is(Integer.parseInt(expectedStatus)));
    }

    private void changeStatus(long id, String role, String email, String newStatus, int expectedHttp) throws Exception {
        mockMvc.perform(patch("/api/rides/" + id + "/status")
                        .header("Authorization", bearer(email, role))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"" + newStatus + "\"}"))
                .andExpect(status().is(expectedHttp));
    }

    private RideStatus dbStatus(long id) {
        return rideRepository.findById(id).orElseThrow().getStatus();
    }

    // ---------- successful workflow ----------

    @Test
    @DisplayName("Happy path: REQUESTED -> ASSIGNED -> ACCEPTED -> IN_PROGRESS -> COMPLETED, persisted at each step")
    void fullLifecycle_happyPath() throws Exception {
        when(driverLookupService.findEligibleDriver(anyString())).thenReturn(DRIVER);

        long id = createRide(PASSENGER);
        Ride created = rideRepository.findById(id).orElseThrow();
        assertEquals(PASSENGER, created.getPassengerEmail());
        assertNotNull(created.getRequestedAt());

        mockMvc.perform(patch("/api/rides/" + id + "/assign")
                        .header("Authorization", bearer(PASSENGER, "PASSENGER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ASSIGNED"))
                .andExpect(jsonPath("$.driverEmail").value(DRIVER));
        assertEquals(RideStatus.ASSIGNED, dbStatus(id));
        assertEquals(DRIVER, rideRepository.findById(id).orElseThrow().getDriverEmail());

        changeStatus(id, "DRIVER", DRIVER, "ACCEPTED", 200);
        assertEquals(RideStatus.ACCEPTED, dbStatus(id));

        changeStatus(id, "DRIVER", DRIVER, "IN_PROGRESS", 200);
        assertEquals(RideStatus.IN_PROGRESS, dbStatus(id));

        changeStatus(id, "DRIVER", DRIVER, "COMPLETED", 200);
        assertEquals(RideStatus.COMPLETED, dbStatus(id));

        mockMvc.perform(get("/api/rides/" + id).header("Authorization", bearer(PASSENGER, "PASSENGER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.passengerEmail").value(PASSENGER))
                .andExpect(jsonPath("$.driverEmail").value(DRIVER));
    }

    @Test
    @DisplayName("Cancellation is allowed from REQUESTED")
    void cancel_fromRequested() throws Exception {
        long id = createRide(PASSENGER);

        changeStatus(id, "PASSENGER", PASSENGER, "CANCELLED", 200);

        assertEquals(RideStatus.CANCELLED, dbStatus(id));
    }

    @Test
    @DisplayName("Cancellation is allowed mid-ride (IN_PROGRESS)")
    void cancel_fromInProgress() throws Exception {
        when(driverLookupService.findEligibleDriver(anyString())).thenReturn(DRIVER);
        long id = createRide(PASSENGER);
        assign(id, "200");
        changeStatus(id, "DRIVER", DRIVER, "ACCEPTED", 200);
        changeStatus(id, "DRIVER", DRIVER, "IN_PROGRESS", 200);

        changeStatus(id, "PASSENGER", PASSENGER, "CANCELLED", 200);

        assertEquals(RideStatus.CANCELLED, dbStatus(id));
    }

    @Test
    @DisplayName("Ride listings are filtered per passenger and per driver")
    void listings_areFilteredByOwner() throws Exception {
        when(driverLookupService.findEligibleDriver(anyString())).thenReturn(DRIVER);
        long aliceRide = createRide(PASSENGER);
        createRide("carol.passenger@test.com");
        assign(aliceRide, "200");

        mockMvc.perform(get("/api/rides/passenger/" + PASSENGER)
                        .header("Authorization", bearer(PASSENGER, "PASSENGER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].passengerEmail").value(PASSENGER));

        mockMvc.perform(get("/api/rides/driver/" + DRIVER)
                        .header("Authorization", bearer(DRIVER, "DRIVER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(aliceRide));
    }

    // ---------- negative scenarios ----------

    @Test
    @DisplayName("Negative: no available driver -> 409 and ride stays REQUESTED")
    void assign_noDriver_rideUnchanged() throws Exception {
        when(driverLookupService.findEligibleDriver(anyString())).thenReturn(null);
        long id = createRide(PASSENGER);

        mockMvc.perform(patch("/api/rides/" + id + "/assign")
                        .header("Authorization", bearer(PASSENGER, "PASSENGER")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("No available driver found for this pickup location"));

        Ride ride = rideRepository.findById(id).orElseThrow();
        assertEquals(RideStatus.REQUESTED, ride.getStatus());
        assertNull(ride.getDriverEmail());
    }

    @Test
    @DisplayName("Negative: cannot assign a driver twice")
    void assign_twice_returns409() throws Exception {
        when(driverLookupService.findEligibleDriver(anyString())).thenReturn(DRIVER);
        long id = createRide(PASSENGER);
        assign(id, "200");

        assign(id, "409");

        assertEquals(RideStatus.ASSIGNED, dbStatus(id));
    }

    @Test
    @DisplayName("Negative: REQUESTED -> COMPLETED skips states -> 409, status unchanged")
    void skipStates_returns409() throws Exception {
        long id = createRide(PASSENGER);

        changeStatus(id, "DRIVER", DRIVER, "COMPLETED", 409);

        assertEquals(RideStatus.REQUESTED, dbStatus(id));
    }

    @Test
    @DisplayName("Negative: a COMPLETED ride is terminal and cannot be cancelled")
    void completedRide_cannotBeCancelled() throws Exception {
        when(driverLookupService.findEligibleDriver(anyString())).thenReturn(DRIVER);
        long id = createRide(PASSENGER);
        assign(id, "200");
        changeStatus(id, "DRIVER", DRIVER, "ACCEPTED", 200);
        changeStatus(id, "DRIVER", DRIVER, "IN_PROGRESS", 200);
        changeStatus(id, "DRIVER", DRIVER, "COMPLETED", 200);

        changeStatus(id, "PASSENGER", PASSENGER, "CANCELLED", 409);

        assertEquals(RideStatus.COMPLETED, dbStatus(id));
    }

    @Test
    @DisplayName("Negative: a CANCELLED ride cannot be reopened")
    void cancelledRide_isTerminal() throws Exception {
        long id = createRide(PASSENGER);
        changeStatus(id, "PASSENGER", PASSENGER, "CANCELLED", 200);

        changeStatus(id, "DRIVER", DRIVER, "ACCEPTED", 409);
        assign(id, "409");

        assertEquals(RideStatus.CANCELLED, dbStatus(id));
    }

    @Test
    @DisplayName("Negative: unknown ride id -> 404 on get, assign and status update")
    void unknownRide_returns404() throws Exception {
        mockMvc.perform(get("/api/rides/987654").header("Authorization", bearer(PASSENGER, "PASSENGER")))
                .andExpect(status().isNotFound());
        assign(987654L, "404");
        changeStatus(987654L, "PASSENGER", PASSENGER, "CANCELLED", 404);
    }

    @Test
    @DisplayName("Negative: unauthenticated request is rejected and nothing is persisted")
    void unauthenticatedCreate_persistsNothing() throws Exception {
        mockMvc.perform(post("/api/rides")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pickupLocation\":\"A\",\"destinationLocation\":\"B\"}"))
                .andExpect(status().isUnauthorized());

        assertEquals(0, rideRepository.count());
    }
}
