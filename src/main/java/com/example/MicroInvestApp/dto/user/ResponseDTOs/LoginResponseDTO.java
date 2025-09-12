// Purpose: Response sent after successful authentication. Contains user info and authentication token.
package com.example.MicroInvestApp.dto.user.ResponseDTOs;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LoginResponseDTO {

    private String token;

    @JsonProperty("token_type")
    private String tokenType = "Bearer";

    @JsonProperty("expires_in")
    private long expiresIn;

    @JsonProperty("user_info")
    private UserAccountResponseDTO userInfo;

    private String message;

    // Constructors
    public LoginResponseDTO() {}

    public LoginResponseDTO(String token, long expiresIn, UserAccountResponseDTO userInfo) {
        this.token = token;
        this.expiresIn = expiresIn;
        this.userInfo = userInfo;
        this.message = "Login successful";
    }

    // Getters and Setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    public long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(long expiresIn) { this.expiresIn = expiresIn; }

    public UserAccountResponseDTO getUserInfo() { return userInfo; }
    public void setUserInfo(UserAccountResponseDTO userInfo) { this.userInfo = userInfo; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
