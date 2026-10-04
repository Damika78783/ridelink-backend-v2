package lk.sliit.it3130.account_service.service;

import lk.sliit.it3130.account_service.dto.AccountResponse;
import lk.sliit.it3130.account_service.dto.LoginRequest;
import lk.sliit.it3130.account_service.dto.LoginResponse;
import lk.sliit.it3130.account_service.dto.RegisterRequest;
import lk.sliit.it3130.account_service.exception.AccountNotFoundException;
import lk.sliit.it3130.account_service.exception.EmailAlreadyExistsException;
import lk.sliit.it3130.account_service.exception.InvalidCredentialsException;
import lk.sliit.it3130.account_service.exception.InvalidRequestException;
import lk.sliit.it3130.account_service.model.AccountStatus;
import lk.sliit.it3130.account_service.model.Driver;
import lk.sliit.it3130.account_service.model.Passenger;
import lk.sliit.it3130.account_service.repository.AdminRepository;
import lk.sliit.it3130.account_service.repository.DriverRepository;
import lk.sliit.it3130.account_service.repository.PassengerRepository;
import lk.sliit.it3130.account_service.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock private PassengerRepository passengerRepository;
    @Mock private DriverRepository driverRepository;
    @Mock private AdminRepository adminRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;

    @InjectMocks private AccountService accountService;

    // ---------- helpers ----------

    private RegisterRequest registerRequest(String role, String license) {
        RegisterRequest r = new RegisterRequest();
        r.setEmail("user@test.com");
        r.setPassword("Pass@123");
        r.setFullName("Test User");
        r.setPhoneNumber("0771234567");
        r.setRole(role);
        r.setLicenseNumber(license);
        return r;
    }

    private Passenger passenger(AccountStatus status) {
        Passenger p = new Passenger();
        p.setId(1L);
        p.setEmail("user@test.com");
        p.setPassword("hashed");
        p.setFullName("Test User");
        p.setPhoneNumber("0771234567");
        p.setStatus(status);
        return p;
    }

    private LoginRequest loginRequest(String password) {
        LoginRequest l = new LoginRequest();
        l.setEmail("user@test.com");
        l.setPassword(password);
        return l;
    }

    // ---------- registration ----------

    @Test
    void register_passenger_success_hashesPasswordAndReturnsActiveAccount() {
        when(passwordEncoder.encode("Pass@123")).thenReturn("hashed");
        when(passengerRepository.save(any(Passenger.class))).thenAnswer(i -> i.getArgument(0));

        AccountResponse response = accountService.register(registerRequest("PASSENGER", null));

        assertEquals("PASSENGER", response.getRole());
        assertEquals("ACTIVE", response.getStatus());
        assertEquals("user@test.com", response.getEmail());

        ArgumentCaptor<Passenger> captor = ArgumentCaptor.forClass(Passenger.class);
        verify(passengerRepository).save(captor.capture());
        assertEquals("hashed", captor.getValue().getPassword());
    }

    @Test
    void register_driver_success() {
        when(passwordEncoder.encode("Pass@123")).thenReturn("hashed");
        when(driverRepository.save(any(Driver.class))).thenAnswer(i -> i.getArgument(0));

        AccountResponse response = accountService.register(registerRequest("DRIVER", "B1234567"));

        assertEquals("DRIVER", response.getRole());
        verify(driverRepository).save(any(Driver.class));
    }

    @Test
    void register_duplicateEmail_throwsEmailAlreadyExists() {
        when(passengerRepository.existsByEmail("user@test.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class,
                () -> accountService.register(registerRequest("PASSENGER", null)));

        verify(passengerRepository, never()).save(any());
    }

    @Test
    void register_driver_missingLicense_throwsInvalidRequest() {
        assertThrows(InvalidRequestException.class,
                () -> accountService.register(registerRequest("DRIVER", null)));

        verify(driverRepository, never()).save(any());
    }

    @Test
    void register_driver_blankLicense_throwsInvalidRequest() {
        assertThrows(InvalidRequestException.class,
                () -> accountService.register(registerRequest("DRIVER", "   ")));
    }

    @Test
    void register_driver_duplicateLicense_throwsInvalidRequest() {
        when(driverRepository.existsByLicenseNumber("B1234567")).thenReturn(true);

        assertThrows(InvalidRequestException.class,
                () -> accountService.register(registerRequest("DRIVER", "B1234567")));

        verify(driverRepository, never()).save(any());
    }

    // ---------- login ----------

    @Test
    void login_success_returnsTokenAndAccount() {
        when(passengerRepository.findByEmail("user@test.com"))
                .thenReturn(Optional.of(passenger(AccountStatus.ACTIVE)));
        when(passwordEncoder.matches("Pass@123", "hashed")).thenReturn(true);
        when(jwtUtil.generateToken("user@test.com", "PASSENGER")).thenReturn("jwt-token");

        LoginResponse response = accountService.login(loginRequest("Pass@123"));

        assertEquals("jwt-token", response.getToken());
        assertEquals("PASSENGER", response.getAccount().getRole());
    }

    @Test
    void login_wrongPassword_throwsInvalidCredentials() {
        when(passengerRepository.findByEmail("user@test.com"))
                .thenReturn(Optional.of(passenger(AccountStatus.ACTIVE)));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
                () -> accountService.login(loginRequest("wrong")));

        verify(jwtUtil, never()).generateToken(any(), any());
    }

    @Test
    void login_unknownEmail_throwsInvalidCredentials() {
        assertThrows(InvalidCredentialsException.class,
                () -> accountService.login(loginRequest("Pass@123")));
    }

    @Test
    void login_suspendedAccount_isBlocked() {
        when(passengerRepository.findByEmail("user@test.com"))
                .thenReturn(Optional.of(passenger(AccountStatus.SUSPENDED)));
        when(passwordEncoder.matches("Pass@123", "hashed")).thenReturn(true);

        InvalidCredentialsException ex = assertThrows(InvalidCredentialsException.class,
                () -> accountService.login(loginRequest("Pass@123")));

        assertTrue(ex.getMessage().toLowerCase().contains("suspended"));
        verify(jwtUtil, never()).generateToken(any(), any());
    }

    // ---------- profile ----------

    @Test
    void getProfile_unknownEmail_throwsNotFound() {
        assertThrows(AccountNotFoundException.class,
                () -> accountService.getProfile("nobody@test.com"));
    }

    @Test
    void updateProfile_onlyProvidedFieldsChange() {
        Passenger p = passenger(AccountStatus.ACTIVE);
        when(passengerRepository.findByEmail("user@test.com")).thenReturn(Optional.of(p));
        when(passengerRepository.save(any(Passenger.class))).thenAnswer(i -> i.getArgument(0));

        AccountResponse response = accountService.updateProfile("user@test.com", "New Name", null);

        assertEquals("New Name", response.getFullName());
        assertEquals("0771234567", response.getPhoneNumber());
    }

    // ---------- status management ----------

    @Test
    void updateStatus_suspendsPassenger() {
        when(passengerRepository.findByEmail("user@test.com"))
                .thenReturn(Optional.of(passenger(AccountStatus.ACTIVE)));
        when(passengerRepository.save(any(Passenger.class))).thenAnswer(i -> i.getArgument(0));

        AccountResponse response = accountService.updateStatus("user@test.com", AccountStatus.SUSPENDED);

        assertEquals("SUSPENDED", response.getStatus());
    }

    @Test
    void updateStatus_unknownEmail_throwsNotFound() {
        assertThrows(AccountNotFoundException.class,
                () -> accountService.updateStatus("nobody@test.com", AccountStatus.SUSPENDED));
    }

    // ---------- lookup by id (used by other services) ----------

    @Test
    void getDriverById_unknownId_throwsNotFound() {
        when(driverRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> accountService.getDriverById(99L));
    }

    @Test
    void getPassengerById_found_returnsPassengerRole() {
        when(passengerRepository.findById(1L)).thenReturn(Optional.of(passenger(AccountStatus.ACTIVE)));

        assertEquals("PASSENGER", accountService.getPassengerById(1L).getRole());
    }
}