package com.example.MicroInvestApp.impl.usermgmt;

import com.example.MicroInvestApp.domain.user.UserAccount;
import com.example.MicroInvestApp.repositories.user.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;

/**
 * Custom UserDetailsService implementation for Spring Security
 * Loads user-specific data for authentication and authorization
 */
@Service
@Transactional
public class CustomUserDetailsService implements UserDetailsService {

    private final UserAccountRepository userAccountRepository;

    @Autowired
    public CustomUserDetailsService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    /**
     * Load user by username or email for Spring Security authentication
     * This method is called during the authentication process
     *
     * @param usernameOrEmail - username or email provided during login
     * @return UserDetails object containing user information for Spring Security
     * @throws UsernameNotFoundException if user is not found
     */
    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        System.out.println("=".repeat(50));
        System.out.println("loadUserByUsername called with: '" + usernameOrEmail + "'");

        try {
            // First, let's check what users exist in the database
            long totalUsers = userAccountRepository.count();
            System.out.println("Total users in database: " + totalUsers);

            // Try to find by username first
            System.out.println("Searching by username: '" + usernameOrEmail + "'");
            var userByUsername = userAccountRepository.findByUsername(usernameOrEmail);
            System.out.println("Search by username result: " + (userByUsername.isPresent() ? "FOUND" : "NOT FOUND"));

            // Try to find by email
            System.out.println("Searching by email: '" + usernameOrEmail + "'");
            var userByEmail = userAccountRepository.findByEmail(usernameOrEmail);
            System.out.println("Search by email result: " + (userByEmail.isPresent() ? "FOUND" : "NOT FOUND"));

            // Combine results
            UserAccount userAccount = userByUsername
                    .or(() -> userByEmail)
                    .orElse(null);

            if (userAccount == null) {
                System.err.println("USER NOT FOUND: No user found with username or email: " + usernameOrEmail);

                // Let's see what users do exist (for debugging)
                System.out.println("Existing users in database:");
                var allUsers = userAccountRepository.findAll();
                for (UserAccount user : allUsers) {
                    System.out.println("  - ID: " + user.getUserId() +
                            ", Username: '" + user.getUsername() +
                            "', Email: '" + user.getEmail() +
                            "', Status: " + user.getAccountStatus());
                }

                throw new UsernameNotFoundException("User not found with username or email: " + usernameOrEmail);
            }

            System.out.println("USER FOUND:");
            System.out.println("  ID: " + userAccount.getUserId());
            System.out.println("  Username: '" + userAccount.getUsername() + "'");
            System.out.println("  Email: '" + userAccount.getEmail() + "'");
            System.out.println("  Status: " + userAccount.getAccountStatus());
            System.out.println("  Email Verified: " + userAccount.isEmailVerified());

            // Check if account is active
            if (!isAccountActive(userAccount)) {
                System.err.println("ACCOUNT INACTIVE: User account is inactive: " + usernameOrEmail);
                throw new UsernameNotFoundException("User account is inactive: " + usernameOrEmail);
            }

            // FIXED: Use consistent password field
            String password = userAccount.getPasswordHash();
            if (password == null || password.isEmpty()) {
                // Fallback to getPassword() method which should return passwordHash
                password = userAccount.getPassword();
                System.out.println("Using getPassword() method for authentication");
            } else {
                System.out.println("Using getPasswordHash() field for authentication");
            }

            if (password == null || password.isEmpty()) {
                System.err.println("NO PASSWORD: No password found for user: " + usernameOrEmail);
                throw new UsernameNotFoundException("No password configured for user: " + usernameOrEmail);
            }

            System.out.println("Password hash available: YES (length: " + password.length() + ")");

            // Get authorities
            Collection<? extends GrantedAuthority> authorities = getAuthorities(userAccount);
            System.out.println("Assigned authorities: " + authorities);

            // Build Spring Security UserDetails object
            UserDetails userDetails = User.builder()
                    .username(userAccount.getUsername())
                    .password(password)
                    .authorities(authorities)
                    .accountExpired(false)
                    .accountLocked(false)
                    .credentialsExpired(false)
                    .disabled(!isAccountActive(userAccount))
                    .build();

            System.out.println("UserDetails created successfully for: " + userAccount.getUsername());
            System.out.println("=".repeat(50));

            return userDetails;

        } catch (Exception e) {
            System.err.println("Exception in loadUserByUsername: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Get user authorities/roles for Spring Security
     * Currently assigns ROLE_USER to all users, but can be extended for admin roles
     *
     * @param userAccount - the user account
     * @return Collection of GrantedAuthority objects
     */
    private Collection<? extends GrantedAuthority> getAuthorities(UserAccount userAccount) {
        System.out.println("Determining authorities for user: " + userAccount.getUsername() +
                " (email: " + userAccount.getEmail() + ")");

        // More robust admin check - only exact matches
        boolean isAdmin = ("admin".equals(userAccount.getUsername().trim().toLowerCase()) ||
                "admin@microinvest.com".equals(userAccount.getEmail().trim().toLowerCase()));

        if (isAdmin) {
            System.out.println("✅ Assigning ROLE_ADMIN to user: " + userAccount.getUsername());
            return Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }

        System.out.println("✅ Assigning ROLE_USER to user: " + userAccount.getUsername());
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    /**
     * Load user by ID (useful for JWT token validation)
     *
     * @param userId - the user ID
     * @return UserDetails object
     * @throws UsernameNotFoundException if user is not found
     */
    public UserDetails loadUserById(Long userId) throws UsernameNotFoundException {
        UserAccount userAccount = userAccountRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));

        if (!isAccountActive(userAccount)) {
            throw new UsernameNotFoundException("User account is inactive with id: " + userId);
        }

        // FIXED: Use consistent password field
        String password = userAccount.getPasswordHash();
        if (password == null || password.isEmpty()) {
            password = userAccount.getPassword();
        }

        return User.builder()
                .username(userAccount.getUsername())
                .password(password)
                .authorities(getAuthorities(userAccount))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!isAccountActive(userAccount))
                .build();
    }

    /**
     * Check if account is active based on AccountStatus enum
     *
     * @param userAccount - the user account to check
     * @return true if account is active, false otherwise
     */
    private boolean isAccountActive(UserAccount userAccount) {
        boolean isActive = userAccount.getAccountStatus() != null &&
                userAccount.getAccountStatus().name().equals("ACTIVE");
        System.out.println("User " + userAccount.getUsername() + " active status: " + isActive +
                " (AccountStatus: " + userAccount.getAccountStatus() + ")");
        return isActive;
    }
}