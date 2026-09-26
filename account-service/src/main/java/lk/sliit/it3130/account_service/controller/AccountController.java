package lk.sliit.it3130.account_service.controller;

import jakarta.validation.Valid;
import lk.sliit.it3130.account_service.dto.AccountResponse;
import lk.sliit.it3130.account_service.dto.LoginRequest;
import lk.sliit.it3130.account_service.dto.RegisterRequest;
import lk.sliit.it3130.account_service.model.AccountStatus;
import lk.sliit.it3130.account_service.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lk.sliit.it3130.account_service.dto.LoginResponse;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    // POST /api/accounts/register
    @PostMapping("/register")
    public ResponseEntity<AccountResponse> register(@Valid @RequestBody RegisterRequest request) {
        AccountResponse response = accountService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // POST /api/accounts/login
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request){
        return ResponseEntity.ok(accountService.login(request));
    }

    // GET /api/accounts/profile/{email}
    @GetMapping("/profile/{email}")
    public ResponseEntity<AccountResponse> getProfile(@PathVariable String email) {
        return ResponseEntity.ok(accountService.getProfile(email));
    }

    // PUT /api/accounts/profile/{email}
    @PutMapping("/profile/{email}")
    public ResponseEntity<AccountResponse> updateProfile(
            @PathVariable String email,
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String phoneNumber) {
        return ResponseEntity.ok(accountService.updateProfile(email, fullName, phoneNumber));
    }

    // PATCH /api/accounts/status/{email}?status=SUSPENDED
    @PatchMapping("/status/{email}")
    public ResponseEntity<AccountResponse> updateStatus(
            @PathVariable String email,
            @RequestParam AccountStatus status) {
        return ResponseEntity.ok(accountService.updateStatus(email, status));
    }

    // DELETE /api/accounts/{email} (Admin only)
    @DeleteMapping("/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAccount(@PathVariable String email) {
    accountService.deleteAccount(email);
    return ResponseEntity.noContent().build();
}
}