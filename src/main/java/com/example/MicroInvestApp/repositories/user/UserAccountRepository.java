package com.example.MicroInvestApp.repositories.user;


import com.example.MicroInvestApp.domain.enums.AccountStatus;
import com.example.MicroInvestApp.domain.enums.RiskTolerance;
import com.example.MicroInvestApp.domain.user.UserAccount;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    // Basic finder methods
    Optional<UserAccount> findByEmail(String email);
    Optional<UserAccount> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

    // Status-based queries
    List<UserAccount> findByAccountStatus(AccountStatus accountStatus);
    List<UserAccount> findByEmailVerified(boolean emailVerified);

    // Date-based queries
    List<UserAccount> findByCreatedAtAfter(LocalDate date);
    List<UserAccount> findByCreatedAtBetween(LocalDate startDate, LocalDate endDate);

    // Financial queries
    @Query("SELECT u FROM UserAccount u WHERE u.currentVirtualBalance >= :minBalance")
    List<UserAccount> findByMinimumBalance(@Param("minBalance") double minBalance);

    @Query("SELECT u FROM UserAccount u WHERE u.totalInvestedAmount > :amount")
    List<UserAccount> findActiveInvestors(@Param("amount") double amount);

    // Complex queries
    @Query("SELECT u FROM UserAccount u WHERE u.accountStatus = :status AND u.emailVerified = true")
    List<UserAccount> findActiveVerifiedUsers(@Param("status") AccountStatus status);

    // Statistics queries
    @Query("SELECT COUNT(u) FROM UserAccount u WHERE u.createdAt >= :date")
    long countNewUsersFromDate(@Param("date") LocalDate date);

    @Query("SELECT AVG(u.currentVirtualBalance) FROM UserAccount u WHERE u.accountStatus = 'ACTIVE'")
    Double getAverageBalanceForActiveUsers();
}
