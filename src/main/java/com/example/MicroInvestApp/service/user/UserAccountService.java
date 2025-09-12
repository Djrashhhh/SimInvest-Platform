package com.example.MicroInvestApp.service.user;

import com.example.MicroInvestApp.domain.enums.AccountStatus;
import com.example.MicroInvestApp.dto.user.RequestDTOs.UserLoginRequestDTO;
import com.example.MicroInvestApp.dto.user.RequestDTOs.UserRegistrationRequestDTO;
import com.example.MicroInvestApp.dto.user.RequestDTOs.UserUpdateRequestDTO;
import com.example.MicroInvestApp.dto.user.ResponseDTOs.LoginResponseDTO;
import com.example.MicroInvestApp.dto.user.ResponseDTOs.UserAccountResponseDTO;

import java.util.List;
import java.util.Optional;

public interface UserAccountService {

    // Registration and Authentication - Updated method names to match implementation
    UserAccountResponseDTO createUserAccount(UserRegistrationRequestDTO registrationRequest);
    UserAccountResponseDTO getUserByUsernameOrEmail(String usernameOrEmail);
    LoginResponseDTO authenticateUser(UserLoginRequestDTO loginRequest);
    void logoutUser(String token);

    // User Management
    Optional<UserAccountResponseDTO> getUserById(Long userId);
    Optional<UserAccountResponseDTO> getUserByEmail(String email);
    Optional<UserAccountResponseDTO> getUserByUsername(String username);
    UserAccountResponseDTO updateUser(Long userId, UserUpdateRequestDTO updateRequest);
    void deleteUser(Long userId);

    // Account Status Management
    void activateAccount(Long userId);
    void deactivateAccount(Long userId);
    void suspendAccount(Long userId);
    List<UserAccountResponseDTO> getUsersByStatus(AccountStatus status);

    // Financial Operations
    void updateBalance(Long userId, double newBalance);
    void processInvestment(Long userId, double amount);
    void processSale(Long userId, double saleAmount, double gains);

    // Validation and Verification
    boolean isEmailAvailable(String email);
    boolean isUsernameAvailable(String username);
    void verifyEmail(Long userId);
    boolean verifySecurityAnswer(Long userId, String answer);

    // Utility Methods
    List<UserAccountResponseDTO> getAllUsers();
    long getTotalUserCount();
    List<UserAccountResponseDTO> getRecentUsers(int days);
}