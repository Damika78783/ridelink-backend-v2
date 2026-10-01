package lk.sliit.it3130.account_service.service;

import lk.sliit.it3130.account_service.dto.AccountResponse;
import lk.sliit.it3130.account_service.dto.LoginRequest;
import lk.sliit.it3130.account_service.dto.RegisterRequest;
import lk.sliit.it3130.account_service.exception.AccountNotFoundException;
import lk.sliit.it3130.account_service.exception.EmailAlreadyExistsException;
import lk.sliit.it3130.account_service.exception.InvalidCredentialsException;
import lk.sliit.it3130.account_service.exception.InvalidRequestException;
import lk.sliit.it3130.account_service.model.Account;
import lk.sliit.it3130.account_service.model.AccountStatus;
import lk.sliit.it3130.account_service.model.Driver;
import lk.sliit.it3130.account_service.model.Passenger;
import lk.sliit.it3130.account_service.repository.DriverRepository;
import lk.sliit.it3130.account_service.repository.PassengerRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lk.sliit.it3130.account_service.dto.LoginResponse;
import lk.sliit.it3130.account_service.security.JwtUtil;
import lk.sliit.it3130.account_service.model.Admin;
import lk.sliit.it3130.account_service.repository.AdminRepository;
import java.util.ArrayList; 
import java.util.List;

@Service
public class AccountService {

    private final PassengerRepository passengerRepository;
    private final DriverRepository driverRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AccountService(PassengerRepository passengerRepository,
                           DriverRepository driverRepository,
                           AdminRepository adminRepository,
                           PasswordEncoder passwordEncoder,
                           JwtUtil jwtUtil) {
        this.passengerRepository = passengerRepository;
        this.driverRepository = driverRepository;
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;

    }

    // ---------- Registration ----------

    public AccountResponse register(RegisterRequest request) {
        String role = request.getRole().toUpperCase();

        if (emailExists(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already registered: " + request.getEmail());
        }

        return role.equals("DRIVER") ? registerDriver(request) : registerPassenger(request);
    }

    private AccountResponse registerPassenger(RegisterRequest request) {
        Passenger passenger = new Passenger();
        passenger.setEmail(request.getEmail());
        passenger.setPassword(passwordEncoder.encode(request.getPassword()));
        passenger.setFullName(request.getFullName());
        passenger.setPhoneNumber(request.getPhoneNumber());
        passenger.setStatus(AccountStatus.ACTIVE);

        return toResponse(passengerRepository.save(passenger), "PASSENGER");
    }

    private AccountResponse registerDriver(RegisterRequest request) {
        if (request.getLicenseNumber() == null || request.getLicenseNumber().isBlank()) {
            throw new InvalidRequestException("License number is required for driver registration");
        }
        if (driverRepository.existsByLicenseNumber(request.getLicenseNumber())) {
            throw new InvalidRequestException("License number already registered: " + request.getLicenseNumber());
        }

        Driver driver = new Driver();
        driver.setEmail(request.getEmail());
        driver.setPassword(passwordEncoder.encode(request.getPassword()));
        driver.setFullName(request.getFullName());
        driver.setPhoneNumber(request.getPhoneNumber());
        driver.setLicenseNumber(request.getLicenseNumber());
        driver.setStatus(AccountStatus.ACTIVE);

        return toResponse(driverRepository.save(driver), "DRIVER");
    }

    private boolean emailExists(String email) {
    return passengerRepository.existsByEmail(email)
            || driverRepository.existsByEmail(email)
            || adminRepository.existsByEmail(email);
}

    // ---------- Login ----------

   public LoginResponse login(LoginRequest request) {
        Passenger passenger = passengerRepository.findByEmail(request.getEmail()).orElse(null);
        if (passenger != null) {
            return authenticate(passenger, request.getPassword(), "PASSENGER");
        }

        Driver driver = driverRepository.findByEmail(request.getEmail()).orElse(null);
        if (driver != null) {
            return authenticate(driver, request.getPassword(), "DRIVER");
        }
        
        Admin admin = adminRepository.findByEmail(request.getEmail()).orElse(null);
        if (admin != null) {
            return authenticate(admin, request.getPassword(), "ADMIN");
        }

        throw new InvalidCredentialsException("Invalid email or password");
    }

    private LoginResponse authenticate(Account account, String rawPassword, String role) {
        if (!passwordEncoder.matches(rawPassword, account.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new InvalidCredentialsException(
                    "Account is " + account.getStatus().name().toLowerCase() + " — login not allowed");
        }
        AccountResponse response = toResponse(account, role);
        String token = jwtUtil.generateToken(account.getEmail(), role);
        
        return new LoginResponse(token, response);
    }

    // ---------- Profile ----------

    public AccountResponse getProfile(String email) {
    Passenger passenger = passengerRepository.findByEmail(email).orElse(null);
    if (passenger != null) return toResponse(passenger, "PASSENGER");

    Driver driver = driverRepository.findByEmail(email).orElse(null);
    if (driver != null) return toResponse(driver, "DRIVER");

    Admin admin = adminRepository.findByEmail(email).orElse(null);
    if (admin != null) return toResponse(admin, "ADMIN");

    throw new AccountNotFoundException("No account found for email: " + email);
}

public List<AccountResponse> getAllAccounts() {
    List<AccountResponse> all = new ArrayList<>();
    passengerRepository.findAll().forEach(p -> all.add(toResponse(p, "PASSENGER")));
    driverRepository.findAll().forEach(d -> all.add(toResponse(d, "DRIVER")));
    adminRepository.findAll().forEach(a -> all.add(toResponse(a, "ADMIN")));
    return all;
}

public AccountResponse getPassengerById(Long id) {
    return passengerRepository.findById(id)
            .map(p -> toResponse(p, "PASSENGER"))
            .orElseThrow(() -> new AccountNotFoundException("Passenger not found with id: " + id));
}

public AccountResponse getDriverById(Long id) {
    return driverRepository.findById(id)
            .map(d -> toResponse(d, "DRIVER"))
            .orElseThrow(() -> new AccountNotFoundException("Driver not found with id: " + id));
}



    public AccountResponse updateProfile(String email, String fullName, String phoneNumber) {
        Passenger passenger = passengerRepository.findByEmail(email).orElse(null);
        if (passenger != null) {
            applyProfileChanges(passenger, fullName, phoneNumber);
            return toResponse(passengerRepository.save(passenger), "PASSENGER");
        }

        Driver driver = driverRepository.findByEmail(email).orElse(null);
        if (driver != null) {
            applyProfileChanges(driver, fullName, phoneNumber);
            return toResponse(driverRepository.save(driver), "DRIVER");
        }

        throw new AccountNotFoundException("No account found for email: " + email);
    }

    private void applyProfileChanges(Account account, String fullName, String phoneNumber) {
        if (fullName != null && !fullName.isBlank()) account.setFullName(fullName);
        if (phoneNumber != null && !phoneNumber.isBlank()) account.setPhoneNumber(phoneNumber);
    }

    // ---------- Status management ----------

    public AccountResponse updateStatus(String email, AccountStatus newStatus) {
        Passenger passenger = passengerRepository.findByEmail(email).orElse(null);
        if (passenger != null) {
            passenger.setStatus(newStatus);
            return toResponse(passengerRepository.save(passenger), "PASSENGER");
        }

        Driver driver = driverRepository.findByEmail(email).orElse(null);
        if (driver != null) {
            driver.setStatus(newStatus);
            return toResponse(driverRepository.save(driver), "DRIVER");
        }

        throw new AccountNotFoundException("No account found for email: " + email);
    }

    // ---------- Delete (Admin only) ----------

public void deleteAccount(String email) {
    Passenger passenger = passengerRepository.findByEmail(email).orElse(null);
    if (passenger != null) {
        passengerRepository.delete(passenger);
        return;
    }

    Driver driver = driverRepository.findByEmail(email).orElse(null);
    if (driver != null) {
        driverRepository.delete(driver);
        return;
    }

    throw new AccountNotFoundException("No account found for email: " + email);
}

    // ---------- Mapping ----------

    private AccountResponse toResponse(Account account, String role) {
        return new AccountResponse(
                account.getId(),
                account.getEmail(),
                account.getFullName(),
                account.getPhoneNumber(),
                role,
                account.getStatus().name()
        );
    }
}