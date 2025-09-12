package com.example.MicroInvestApp.security;

import com.example.MicroInvestApp.repositories.portfolio.PortfolioRepository;
import com.example.MicroInvestApp.repositories.user.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("portfolioSecurityService")
public class PortfolioSecurityService {

    private final PortfolioRepository portfolioRepository;
    private final UserAccountRepository userAccountRepository;

    @Autowired
    public PortfolioSecurityService(PortfolioRepository portfolioRepository,
                                    UserAccountRepository userAccountRepository) {
        this.portfolioRepository = portfolioRepository;
        this.userAccountRepository = userAccountRepository;
    }

    /**
     * Check if the authenticated user can access portfolios for the specified user ID
     */
    public boolean canAccessUserPortfolio(String username, Long userId) {
        try {
            // Get user account by username
            return userAccountRepository.findByUsername(username)
                    .map(userAccount -> userAccount.getUserId().equals(userId))
                    .orElse(false);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if the authenticated user owns the specified portfolio
     */
    public boolean canAccessPortfolio(String username, Long portfolioId) {
        try {
            return portfolioRepository.findById(portfolioId)
                    .map(portfolio -> portfolio.getUserAccount().getUsername().equals(username))
                    .orElse(false);
        } catch (Exception e) {
            return false;
        }
    }
}
