package lk.sliit.it3130.account_service.controller;

import jakarta.validation.Valid;
import lk.sliit.it3130.account_service.dto.*;
import lk.sliit.it3130.account_service.model.AccountStatus;
import lk.sliit.it3130.account_service.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    // POST /api/accounts/register (public)
    @PostMapping("/register")
    public ResponseEntity<AccountResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.register(request));
    }

    // POST /api/accounts/login (public)
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(accountService.login(request));
    }

    // GET /api/accounts/me (any logged-in user)
    @GetMapping("/me")
    public ResponseEntity<AccountResponse> getMyProfile(Authentication auth) {
        return ResponseEntity.ok(accountService.getProfile(auth.getName()));
    }

    // PUT /api/accounts/me (any logged-in user)
    @PutMapping("/me")
    public ResponseEntity<AccountResponse> updateMyProfile(Authentication auth,
                                                           @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(
                accountService.updateProfile(auth.getName(), request.getFullName(), request.getPhoneNumber()));
    }

    // GET /api/accounts (ADMIN)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AccountResponse>> getAllAccounts() {
        return ResponseEntity.ok(accountService.getAllAccounts());
    }

    // GET /api/accounts/passengers/{id} (used by other services)
    @GetMapping("/passengers/{id}")
    public ResponseEntity<AccountResponse> getPassengerById(@PathVariable Long id) {
        return ResponseEntity.ok(accountService.getPassengerById(id));
    }

    // GET /api/accounts/drivers/{id} (used by other services)
    @GetMapping("/drivers/{id}")
    public ResponseEntity<AccountResponse> getDriverById(@PathVariable Long id) {
        return ResponseEntity.ok(accountService.getDriverById(id));
    }

    // PATCH /api/accounts/status/{email}?status=SUSPENDED (ADMIN)
    @PatchMapping("/status/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AccountResponse> updateStatus(@PathVariable String email,
                                                        @RequestParam AccountStatus status) {
        return ResponseEntity.ok(accountService.updateStatus(email, status));
    }

    // DELETE /api/accounts/{email} (ADMIN)
    @DeleteMapping("/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAccount(@PathVariable String email) {
        accountService.deleteAccount(email);
        return ResponseEntity.noContent().build();
    }
}