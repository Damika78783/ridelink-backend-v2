package lk.sliit.it3130.account_service.config;

import lk.sliit.it3130.account_service.model.AccountStatus;
import lk.sliit.it3130.account_service.model.Admin;
import lk.sliit.it3130.account_service.repository.AdminRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public AdminSeeder(AdminRepository adminRepository,
                       PasswordEncoder passwordEncoder,
                       @Value("${admin.email}") String adminEmail,
                       @Value("${admin.password}") String adminPassword) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        if (!adminRepository.existsByEmail(adminEmail)) {
            Admin admin = new Admin();
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setFullName("System Admin");
            admin.setPhoneNumber("0000000000");
            admin.setStatus(AccountStatus.ACTIVE);
            adminRepository.save(admin);
            log.info("Seeded default admin account: {}", adminEmail);
        }
    }
}