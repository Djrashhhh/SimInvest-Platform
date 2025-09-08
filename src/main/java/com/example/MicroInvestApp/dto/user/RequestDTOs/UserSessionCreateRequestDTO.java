package com.example.MicroInvestApp.dto.user.RequestDTOs;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;


public class UserSessionCreateRequestDTO {
    @NotBlank(message = "IP address is required")
    @JsonProperty("ip_address")
    private String ipAddress;

    @NotBlank(message = "User agent is required")
    @JsonProperty("user_agent")
    private String userAgent;

    @JsonProperty("remember_me")
    private boolean rememberMe = false;

    // Constructors, getters, and setters
    public UserSessionCreateRequestDTO() {}

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public boolean isRememberMe() { return rememberMe; }
    public void setRememberMe(boolean rememberMe) { this.rememberMe = rememberMe; }
}
