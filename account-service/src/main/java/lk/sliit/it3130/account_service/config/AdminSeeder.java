package lk.sliit.it3130.account_service.config;

import lk.sliit.it3130.account_service.model.Admin;
import lk.sliit.it3130.account_service.model.AccountStatus;
import lk.sliit.it3130.account_service.repository.AdminRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminSeeder implements CommandLineRunner {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminSeeder(AdminRepository adminRepository, PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        String adminEmail = "admin@ridelink.com";

        if (!adminRepository.existsByEmail(adminEmail)) {
            Admin admin = new Admin();
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFullName("System Admin");
            admin.setPhoneNumber("0000000000");
            admin.setStatus(AccountStatus.ACTIVE);
            adminRepository.save(admin);
            System.out.println("Seeded default admin account: " + adminEmail);
        }
    }
}