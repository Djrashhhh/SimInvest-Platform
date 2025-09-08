package com.example.MicroInvestApp.controller;

import com.example.MicroInvestApp.config.JwtUtil;
import com.example.MicroInvestApp.domain.user.UserAccount;
import com.example.MicroInvestApp.dto.user.RequestDTOs.UserLoginRequestDTO;
import com.example.MicroInvestApp.dto.user.RequestDTOs.UserRegistrationRequestDTO;
import com.example.MicroInvestApp.dto.user.ResponseDTOs.LoginResponseDTO;
import com.example.MicroInvestApp.dto.user.ResponseDTOs.UserAccountResponseDTO;
import com.example.MicroInvestApp.service.user.UserAccountService;
import com.example.MicroInvestApp.impl.usermgmt.CustomUserDetailsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Authentication Controller handling login and registration endpoints
 * Provides JWT-based authentication for the application
 */
@RestController
@RequestMapping("/api/v1/auth")
// Remove the problematic @CrossOrigin annotation - let CorsConfig handle it
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final UserAccountService userAccountService;


    @Autowired
    public AuthController(AuthenticationManager authenticationManager,
                          CustomUserDetailsService userDetailsService,
                          JwtUtil jwtUtil,
                          UserAccountService userAccountService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.userAccountService = userAccountService;
    }

    /**
     * User login endpoint
     * Authenticates user credentials and returns JWT token
     *
     * @param loginRequest - login credentials (username/email and password)
     * @return LoginResponseDTO with JWT token and user information
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UserLoginRequestDTO loginRequest) {
        System.out.println("=".repeat(60));
        System.out.println("LOGIN ATTEMPT for: " + loginRequest.getUsernameOrEmail());

        try {
            // Authenticate user credentials
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsernameOrEmail(),
                            loginRequest.getPassword()
                    )
            );

            System.out.println("Authentication successful for: " + loginRequest.getUsernameOrEmail());

            // Load user details
            UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUsernameOrEmail());

            // Generate JWT token
            String jwt = jwtUtil.generateToken(userDetails);

            // Get user account information
            UserAccountResponseDTO userInfo = userAccountService.getUserByUsernameOrEmail(loginRequest.getUsernameOrEmail());

            // Create response
            LoginResponseDTO response = new LoginResponseDTO(jwt, jwtUtil.getExpirationTime(), userInfo);

            System.out.println("Login successful for user: " + userDetails.getUsername());   // Log successful login
            System.out.println("=".repeat(60));

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            System.err.println("LOGIN FAILED - Bad credentials for: " + loginRequest.getUsernameOrEmail());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid username/email or password");
            error.put("message", "Authentication failed");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);

        } catch (Exception e) {
            System.err.println("LOGIN FAILED - Unexpected error for: " + loginRequest.getUsernameOrEmail());
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Authentication failed");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * User registration endpoint
     * Creates new user account and returns JWT token
     *
     * @param registrationRequest - user registration information
     * @return LoginResponseDTO with JWT token and user information
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserRegistrationRequestDTO registrationRequest) {
        System.out.println("=".repeat(60));
        System.out.println("REGISTRATION ATTEMPT for username: " + registrationRequest.getUsername() +
                ", email: " + registrationRequest.getEmail());

        try {
            // Log registration details (excluding password)
            System.out.println("Registration details:");
            System.out.println("  - First Name: " + registrationRequest.getFirstName());
            System.out.println("  - Last Name: " + registrationRequest.getLastName());
            System.out.println("  - Username: " + registrationRequest.getUsername());
            System.out.println("  - Email: " + registrationRequest.getEmail());
            System.out.println("  - Risk Tolerance: " + registrationRequest.getRiskTolerance());
            System.out.println("  - Security Question: " + registrationRequest.getSecurityQuestion());
            System.out.println("  - Account Currency: " + registrationRequest.getAccountCurrency());
            System.out.println("  - Initial Balance: " + registrationRequest.getInitialVirtualBalance());
            System.out.println("  - Password provided: " + (registrationRequest.getPassword() != null && !registrationRequest.getPassword().isEmpty()));

            // Create new user account
            System.out.println("Creating user account...");
            UserAccountResponseDTO userAccount = userAccountService.createUserAccount(registrationRequest);
            System.out.println("User account created successfully with ID: " + userAccount.getUserId());

            // Load user details for JWT generation
            System.out.println("Loading user details for JWT generation...");
            UserDetails userDetails = userDetailsService.loadUserByUsername(userAccount.getUsername());
            System.out.println("User details loaded successfully");

            // Generate JWT token
            System.out.println("Generating JWT token...");
            String jwt = jwtUtil.generateToken(userDetails);
            System.out.println("JWT token generated successfully");

            // Create response
            LoginResponseDTO response = new LoginResponseDTO(jwt, jwtUtil.getExpirationTime(), userAccount);
            response.setMessage("Registration successful");

            System.out.println("Registration completed successfully for user: " + userAccount.getUsername());
            System.out.println("=".repeat(60));

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            System.err.println("REGISTRATION FAILED - Validation error: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Registration failed");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);

        } catch (Exception e) {
            System.err.println("REGISTRATION FAILED - Unexpected error for user: " + registrationRequest.getUsername());
            System.err.println("Error type: " + e.getClass().getSimpleName());
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();

            Map<String, String> error = new HashMap<>();
            error.put("error", "Registration failed");
            error.put("message", "An error occurred during registration: " + e.getMessage());
            error.put("errorType", e.getClass().getSimpleName());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Token validation endpoint
     * Validates if the provided JWT token is still valid
     *
     * @param authHeader - Authorization header containing Bearer token
     * @return Validation result
     */
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                if (jwtUtil.validateToken(token)) {
                    String username = jwtUtil.getUsernameFromToken(token);
                    Map<String, Object> response = new HashMap<>();
                    response.put("valid", true);
                    response.put("username", username);
                    response.put("message", "Token is valid");
                    return ResponseEntity.ok(response);
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("valid", false);
            response.put("message", "Invalid token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("valid", false);
            response.put("message", "Token validation failed");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    /**
     * Logout endpoint (client-side token invalidation)
     * In a stateless JWT system, logout is typically handled client-side
     *
     * @return Success message
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Logout successful. Please remove the token from client storage.");
        return ResponseEntity.ok(response);

    }

    /**
     * Get current authenticated user information
     * Returns the full user account details for the authenticated user
     *
     * @param authentication - Spring Security authentication object containing user details
     * @return UserAccountResponseDTO with complete user information
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        try {
            System.out.println("=".repeat(60));
            System.out.println("GET CURRENT USER REQUEST");

            if (authentication == null || authentication.getName() == null) {
                System.err.println("No authentication information found");
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                error.put("message", "No authentication information found");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            String username = authentication.getName();
            System.out.println("Getting user details for username: " + username);

            // Get user by username
            Optional<UserAccountResponseDTO> userOptional = userAccountService.getUserByUsername(username);

            if (userOptional.isEmpty()) {
                System.err.println("User not found with username: " + username);
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not found");
                error.put("message", "User not found with username: " + username);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            UserAccountResponseDTO user = userOptional.get();
            System.out.println("Successfully retrieved user: " + user.getUsername() + " (ID: " + user.getUserId() + ")");
            System.out.println("=".repeat(60));

            return ResponseEntity.ok(user);

        } catch (Exception e) {
            System.err.println("GET CURRENT USER FAILED - Error: " + e.getMessage());
            e.printStackTrace();

            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get current user");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}