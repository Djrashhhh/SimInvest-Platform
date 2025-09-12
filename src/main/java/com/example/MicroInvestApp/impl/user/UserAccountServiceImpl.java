// Purpose: Implementation of user account business logic with proper password handling
package com.example.MicroInvestApp.impl.user;

import com.example.MicroInvestApp.domain.user.UserAccount;
import com.example.MicroInvestApp.dto.user.RequestDTOs.UserLoginRequestDTO;
import com.example.MicroInvestApp.dto.user.RequestDTOs.UserRegistrationRequestDTO;
import com.example.MicroInvestApp.dto.user.RequestDTOs.UserUpdateRequestDTO;
import com.example.MicroInvestApp.dto.user.ResponseDTOs.LoginResponseDTO;
import com.example.MicroInvestApp.dto.user.ResponseDTOs.UserAccountResponseDTO;
import com.example.MicroInvestApp.repositories.user.UserAccountRepository;
import com.example.MicroInvestApp.service.user.UserAccountService;
import com.example.MicroInvestApp.util.UserMapper;
import com.example.MicroInvestApp.exception.user.*;
import com.example.MicroInvestApp.domain.enums.AccountStatus;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserAccountServiceImpl implements UserAccountService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Autowired
    public UserAccountServiceImpl(UserAccountRepository userAccountRepository,
                                  PasswordEncoder passwordEncoder,
                                  UserMapper userMapper) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    @Override
    public UserAccountResponseDTO createUserAccount(UserRegistrationRequestDTO registrationRequest) {
        System.out.println("=".repeat(60));
        System.out.println("CREATING USER ACCOUNT");
        System.out.println("Username: " + registrationRequest.getUsername());
        System.out.println("Email: " + registrationRequest.getEmail());

        try {
            // Validate input
            validateRegistrationRequest(registrationRequest);

            // Check uniqueness
            System.out.println("Checking email availability...");
            if (!isEmailAvailable(registrationRequest.getEmail())) {
                throw new IllegalArgumentException("Email already exists: " + registrationRequest.getEmail());
            }
            System.out.println("Email is available");

            System.out.println("Checking username availability...");
            if (!isUsernameAvailable(registrationRequest.getUsername())) {
                throw new IllegalArgumentException("Username already exists: " + registrationRequest.getUsername());
            }
            System.out.println("Username is available");

            // Create new user account
            System.out.println("Creating new UserAccount entity...");
            UserAccount userAccount = new UserAccount();

            // Set basic information
            userAccount.setFirstName(registrationRequest.getFirstName());
            userAccount.setLastName(registrationRequest.getLastName());
            userAccount.setEmail(registrationRequest.getEmail());
            userAccount.setUsername(registrationRequest.getUsername());

            // CRITICAL: Properly encode password
            System.out.println("Encoding password...");
            String encodedPassword = passwordEncoder.encode(registrationRequest.getPassword());
            userAccount.setPasswordHash(encodedPassword);
            System.out.println("Password encoded successfully (length: " + encodedPassword.length() + ")");

            // Set user preferences
            userAccount.setRiskTolerance(registrationRequest.getRiskTolerance());
            userAccount.setSecurityQuestion(registrationRequest.getSecurityQuestion());

            // CRITICAL: Use the entity's setSecurityAnswer method which handles hashing internally
            System.out.println("Setting security answer...");
            userAccount.setSecurityAnswer(registrationRequest.getSecurityAnswer());
            System.out.println("Security answer set and hashed");

            // Set financial information
            userAccount.setAccountCurrency(registrationRequest.getAccountCurrency());
            userAccount.setInitialVirtualBalance(registrationRequest.getInitialVirtualBalance());
            userAccount.setCurrentVirtualBalance(registrationRequest.getInitialVirtualBalance());

            // Set account status
            userAccount.setAccountStatus(AccountStatus.ACTIVE);
            userAccount.setEmailVerified(false); // Start with unverified email

            System.out.println("All fields set, saving to database...");

            // Save user to database
            UserAccount savedUser = userAccountRepository.save(userAccount);
            System.out.println("User saved successfully with ID: " + savedUser.getUserId());

            // Convert to response DTO
            UserAccountResponseDTO responseDTO = convertToResponseDTO(savedUser);

            System.out.println("User account created successfully!");
            System.out.println("=".repeat(60));

            return responseDTO;

        } catch (Exception e) {
            System.err.println("ERROR creating user account: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public UserAccountResponseDTO getUserByUsernameOrEmail(String usernameOrEmail) {
        System.out.println("Looking up user by username or email: " + usernameOrEmail);

        Optional<UserAccount> userOptional = userAccountRepository.findByUsername(usernameOrEmail)
                .or(() -> userAccountRepository.findByEmail(usernameOrEmail));

        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found: " + usernameOrEmail);
        }

        UserAccount user = userOptional.get();
        System.out.println("User found: " + user.getUsername());

        return convertToResponseDTO(user);
    }

    @Override
    public LoginResponseDTO authenticateUser(UserLoginRequestDTO loginRequest) {
        System.out.println("Authenticating user: " + loginRequest.getUsernameOrEmail());

        // Find user by email or username
        Optional<UserAccount> userOpt = userAccountRepository.findByEmail(loginRequest.getUsernameOrEmail());
        if (userOpt.isEmpty()) {
            userOpt = userAccountRepository.findByUsername(loginRequest.getUsernameOrEmail());
        }

        if (userOpt.isEmpty()) {
            throw new InvalidCredentialsException("Invalid username/email or password");
        }

        UserAccount user = userOpt.get();

        // Verify password using the passwordHash field
        String storedPassword = user.getPasswordHash();
        if (storedPassword == null || storedPassword.isEmpty()) {
            // Fallback to getPassword() method
            storedPassword = user.getPassword();
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(), storedPassword)) {
            throw new InvalidCredentialsException("Invalid username/email or password");
        }

        // Check account status
        if (user.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new AccountDeactivatedException("Account is not active");
        }

        System.out.println("User authenticated successfully: " + user.getUsername());

        // TODO: Generate JWT token (this should be handled in AuthController)
        String token = "generated_jwt_token"; // Replace with actual JWT generation
        long expiresIn = 3600; // 1 hour

        UserAccountResponseDTO userInfo = convertToResponseDTO(user);
        return new LoginResponseDTO(token, expiresIn, userInfo);
    }

    // Validation method
    private void validateRegistrationRequest(UserRegistrationRequestDTO request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (request.getPassword() == null || request.getPassword().length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }
        if (request.getFirstName() == null || request.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("First name cannot be empty");
        }
        if (request.getLastName() == null || request.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Last name cannot be empty");
        }
        if (request.getRiskTolerance() == null) {
            throw new IllegalArgumentException("Risk tolerance must be specified");
        }
        if (request.getSecurityQuestion() == null) {
            throw new IllegalArgumentException("Security question must be specified");
        }
        if (request.getSecurityAnswer() == null || request.getSecurityAnswer().trim().isEmpty()) {
            throw new IllegalArgumentException("Security answer cannot be empty");
        }
    }

    // Helper method to convert UserAccount to UserAccountResponseDTO
    private UserAccountResponseDTO convertToResponseDTO(UserAccount userAccount) {
        UserAccountResponseDTO dto = new UserAccountResponseDTO();
        dto.setUserId(userAccount.getUserId());
        dto.setUsername(userAccount.getUsername());
        dto.setEmail(userAccount.getEmail());
        dto.setFirstName(userAccount.getFirstName());
        dto.setLastName(userAccount.getLastName());
        dto.setRiskTolerance(userAccount.getRiskTolerance());
        dto.setAccountStatus(userAccount.getAccountStatus());
        dto.setActive(userAccount.isActive());
        dto.setEmailVerified(userAccount.isEmailVerified());
        dto.setAccountCurrency(userAccount.getAccountCurrency());
        dto.setCurrentVirtualBalance(userAccount.getCurrentVirtualBalance());
        dto.setTotalInvestedAmount(userAccount.getTotalInvestedAmount());
        dto.setTotalReturns(userAccount.getTotalReturns());
        dto.setCreatedAt(userAccount.getCreatedAt());
        dto.setUpdatedAt(userAccount.getUpdatedAt());

        // Set calculated fields
        dto.setFullName(userAccount.getFullName());
        dto.setNetWorth(userAccount.getNetWorth());
        dto.setReturnOnInvestment(userAccount.getReturnOnInvestment());

        return dto;
    }

    // Rest of your existing methods remain the same...

    @Override
    public void logoutUser(String token) {
        // TODO: Invalidate token/session
        // sessionService.invalidateSession(token);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserAccountResponseDTO> getUserById(Long userId) {
        return userAccountRepository.findById(userId)
                .map(this::convertToResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserAccountResponseDTO> getUserByEmail(String email) {
        return userAccountRepository.findByEmail(email)
                .map(this::convertToResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserAccountResponseDTO> getUserByUsername(String username) {
        return userAccountRepository.findByUsername(username)
                .map(this::convertToResponseDTO);
    }

    @Override
    public UserAccountResponseDTO updateUser(Long userId, UserUpdateRequestDTO updateRequest) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        // Update fields
        if (updateRequest.getEmail() != null) {
            user.setEmail(updateRequest.getEmail());
        }
        if (updateRequest.getPassword() != null) {
            // CRITICAL: Encode password when updating
            user.setPasswordHash(passwordEncoder.encode(updateRequest.getPassword()));
        }
        if (updateRequest.getSecurityQuestion() != null) {
            user.setSecurityQuestion(updateRequest.getSecurityQuestion());
        }
        if (updateRequest.getSecurityAnswer() != null) {
            user.setSecurityAnswer(updateRequest.getSecurityAnswer());
        }
        if (updateRequest.getRiskTolerance() != null) {
            user.setRiskTolerance(updateRequest.getRiskTolerance());
        }

        UserAccount updatedUser = userAccountRepository.save(user);
        return convertToResponseDTO(updatedUser);
    }

    @Override
    public void deleteUser(Long userId) {
        if (!userAccountRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found with ID: " + userId);
        }
        userAccountRepository.deleteById(userId);
    }

    @Override
    public void activateAccount(Long userId) {
        updateAccountStatus(userId, AccountStatus.ACTIVE);
    }

    @Override
    public void deactivateAccount(Long userId) {
        updateAccountStatus(userId, AccountStatus.INACTIVE);
    }

    @Override
    public void suspendAccount(Long userId) {
        updateAccountStatus(userId, AccountStatus.SUSPENDED);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserAccountResponseDTO> getUsersByStatus(AccountStatus status) {
        return userAccountRepository.findByAccountStatus(status)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void updateBalance(Long userId, double newBalance) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        user.setCurrentVirtualBalance(newBalance);
        userAccountRepository.save(user);
    }

    @Override
    public void processInvestment(Long userId, double amount) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        user.updateBalanceAfterInvestment(amount);
        userAccountRepository.save(user);
    }

    @Override
    public void processSale(Long userId, double saleAmount, double gains) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        user.updateBalanceAfterSale(saleAmount, gains);
        userAccountRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEmailAvailable(String email) {
        return !userAccountRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isUsernameAvailable(String username) {
        return !userAccountRepository.existsByUsername(username);
    }

    @Override
    public void verifyEmail(Long userId) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        user.setEmailVerified(true);
        userAccountRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean verifySecurityAnswer(Long userId, String answer) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        return user.verifySecurityAnswer(answer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserAccountResponseDTO> getAllUsers() {
        return userAccountRepository.findAll()
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalUserCount() {
        return userAccountRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserAccountResponseDTO> getRecentUsers(int days) {
        LocalDate cutoffDate = LocalDate.now().minusDays(days);
        return userAccountRepository.findByCreatedAtAfter(cutoffDate)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    // Private helper methods
    private void updateAccountStatus(Long userId, AccountStatus status) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        user.setAccountStatus(status);
        userAccountRepository.save(user);
    }
}