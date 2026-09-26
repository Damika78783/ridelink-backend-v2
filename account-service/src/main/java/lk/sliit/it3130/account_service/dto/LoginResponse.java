package lk.sliit.it3130.account_service.dto;

public class LoginResponse {
    private String token;
    private AccountResponse account;

    public LoginResponse(String token, AccountResponse account) {
        this.token = token;
        this.account = account;
    }

    public String getToken() {
        return token;
    }

    public AccountResponse getAccount() {
        return account;
    }
}