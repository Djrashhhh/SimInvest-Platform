package com.example.MicroInvestApp.security;

import com.example.MicroInvestApp.repositories.user.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("userSecurityService")
public class UserSecurityService {

    private final UserAccountRepository userAccountRepository;

    @Autowired
    public UserSecurityService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    /**
     * Check if the authenticated user can access data for the specified user ID
     */
    public boolean canAccessUser(String username, Long userId) {
        try {
            return userAccountRepository.findByUsername(username)
                    .map(userAccount -> userAccount.getUserId().equals(userId))
                    .orElse(false);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if the authenticated user can access their own account
     */
    public boolean isAccountOwner(String username, String targetUsername) {
        return username.equals(targetUsername);
    }
}

// Then you could use it like this in your controllers (if you want stricter security):
// @PreAuthorize("hasRole('USER') and @userSecurityService.canAccessUser(authentication.name, #userId)")