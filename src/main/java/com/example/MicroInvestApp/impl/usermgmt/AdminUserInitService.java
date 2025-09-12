package com.example.MicroInvestApp.impl.usermgmt;

import com.example.MicroInvestApp.domain.user.UserAccount;
import com.example.MicroInvestApp.domain.enums.AccountStatus;
import com.example.MicroInvestApp.domain.enums.Currency;
import com.example.MicroInvestApp.domain.enums.RiskTolerance;
import com.example.MicroInvestApp.domain.enums.SecurityQuestion;
import com.example.MicroInvestApp.repositories.user.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Service to initialize admin user on application startup
 * Implements ApplicationRunner to run after application context is loaded
 */
@Service
public class AdminUserInitService implements ApplicationRunner {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AdminUserInitService(UserAccountRepository userAccountRepository,
                                PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Create admin user if it doesn't exist
     * This runs automatically when the application starts
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        createAdminUserIfNotExists();
    }

    /**
     * Create admin user with default credentials
     * Username: admin
     * Email: admin@microinvest.com
     * Password: Admin123!
     */
    private void createAdminUserIfNotExists() {
        try {
            System.out.println("=".repeat(60));
            System.out.println("CHECKING FOR ADMIN USER...");

            // Check if admin user already exists
            var adminUserOpt = userAccountRepository.findByUsername("admin");
            System.out.println("Admin user search result: " + (adminUserOpt.isPresent() ? "FOUND" : "NOT FOUND"));

            if (adminUserOpt.isEmpty()) {
                System.out.println("Creating admin user...");

                // Create admin user account
                UserAccount adminUser = new UserAccount();
                adminUser.setFirstName("Admin");
                adminUser.setLastName("User");
                adminUser.setEmail("admin@microinvest.com");
                adminUser.setUsername("admin");

                // IMPORTANT: Set the password field that will be used for authentication
                String rawPassword = "Admin123!";
                String encodedPassword = passwordEncoder.encode(rawPassword);
                System.out.println("Raw password: " + rawPassword);
                System.out.println("Encoded password: " + encodedPassword);

                adminUser.setPassword(encodedPassword); // This is what Spring Security will check
                adminUser.setPasswordHash(encodedPassword); // Also set the hash field if needed

                adminUser.setAccountStatus(AccountStatus.ACTIVE);
                adminUser.setEmailVerified(true);
                adminUser.setCreatedAt(LocalDate.now());
                adminUser.setUpdatedAt(LocalDateTime.now());
                adminUser.setRiskTolerance(RiskTolerance.MODERATE);
                adminUser.setSecurityQuestion(SecurityQuestion.FIRST_PET_NAME);
                adminUser.setSecurityAnswer("max"); // This will be hashed by the setter method
                adminUser.setAccountCurrency(Currency.USD);
                adminUser.setInitialVirtualBalance(10000.0);
                adminUser.setCurrentVirtualBalance(10000.0);

                // Save admin user
                UserAccount savedUser = userAccountRepository.save(adminUser);
                System.out.println("Admin user saved with ID: " + savedUser.getUserId());

                // Verify the save worked
                var verifyUser = userAccountRepository.findByUsername("admin");
                if (verifyUser.isPresent()) {
                    UserAccount user = verifyUser.get();
                    System.out.println("VERIFICATION - User found after save:");
                    System.out.println("  ID: " + user.getUserId());
                    System.out.println("  Username: " + user.getUsername());
                    System.out.println("  Email: " + user.getEmail());
                    System.out.println("  Status: " + user.getAccountStatus());
                    System.out.println("  Password field: " + (user.getPassword() != null ? "SET" : "NULL"));
                    System.out.println("  PasswordHash field: " + (user.getPasswordHash() != null ? "SET" : "NULL"));
                } else {
                    System.err.println("VERIFICATION FAILED - Admin user not found after save!");
                }

                System.out.println("=".repeat(60));
                System.out.println("ADMIN USER CREATED SUCCESSFULLY!");
                System.out.println("Username: admin");
                System.out.println("Email: admin@microinvest.com");
                System.out.println("Password: Admin123!");
                System.out.println("=".repeat(60));

            } else {
                UserAccount existingUser = adminUserOpt.get();
                System.out.println("Admin user already exists:");
                System.out.println("  ID: " + existingUser.getUserId());
                System.out.println("  Username: " + existingUser.getUsername());
                System.out.println("  Email: " + existingUser.getEmail());
                System.out.println("  Status: " + existingUser.getAccountStatus());
                System.out.println("  Password field: " + (existingUser.getPassword() != null ? "SET" : "NULL"));
                System.out.println("  PasswordHash field: " + (existingUser.getPasswordHash() != null ? "SET" : "NULL"));
                System.out.println("=".repeat(60));
            }

        } catch (Exception e) {
            System.err.println("Error creating admin user: " + e.getMessage());
            e.printStackTrace();
        }
    }
}