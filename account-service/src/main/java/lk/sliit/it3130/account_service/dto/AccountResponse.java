package lk.sliit.it3130.account_service.dto;

public class AccountResponse {

    private Long id;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String role;
    private String status;

    public AccountResponse(Long id, String email, String fullName, String phoneNumber, String role, String status) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.status = status;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getRole() { return role; }
    public String getStatus() { return status; }
}