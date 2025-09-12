package com.example.MicroInvestApp.util;

import com.example.MicroInvestApp.domain.user.UserAccount;
import com.example.MicroInvestApp.dto.user.RequestDTOs.UserRegistrationRequestDTO;
import com.example.MicroInvestApp.dto.user.ResponseDTOs.UserAccountResponseDTO;
import com.example.MicroInvestApp.dto.user.ResponseDTOs.UserSummaryResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    // Convert Registration DTO to Entity
    public UserAccount toEntity(UserRegistrationRequestDTO dto) {
        UserAccount user = new UserAccount();

        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        user.setUsername(dto.getUsername());
        user.setRiskTolerance(dto.getRiskTolerance());
        user.setSecurityQuestion(dto.getSecurityQuestion());
        user.setSecurityAnswer(dto.getSecurityAnswer());
        user.setAccountCurrency(dto.getAccountCurrency());
        user.setInitialVirtualBalance(dto.getInitialVirtualBalance());
        user.setCurrentVirtualBalance(dto.getInitialVirtualBalance());

        return user;
    }

    // Convert Entity to Response DTO
    public UserAccountResponseDTO toResponseDTO(UserAccount user) {
        UserAccountResponseDTO dto = new UserAccountResponseDTO();

        dto.setUserId(user.getUserId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setUsername(user.getUsername());
        dto.setRiskTolerance(user.getRiskTolerance());
        dto.setAccountStatus(user.getAccountStatus());
        dto.setEmailVerified(user.isEmailVerified());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        dto.setCurrentVirtualBalance(user.getCurrentVirtualBalance());
        dto.setTotalInvestedAmount(user.getTotalInvestedAmount());
        dto.setTotalReturns(user.getTotalReturns());
        dto.setAccountCurrency(user.getAccountCurrency());

        // Set calculated fields
        dto.setFullName(user.getFullName());
        dto.setNetWorth(user.getNetWorth());
        dto.setReturnOnInvestment(user.getReturnOnInvestment());

        return dto;
    }

    // Convert Entity to Summary DTO (lighter version)
    public UserSummaryResponseDTO toSummaryDTO(UserAccount user) {
        UserSummaryResponseDTO dto = new UserSummaryResponseDTO();

        dto.setUserId(user.getUserId());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setUsername(user.getUsername());
        dto.setAccountStatus(user.getAccountStatus());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setNetWorth(user.getNetWorth());

        return dto;
    }
}
