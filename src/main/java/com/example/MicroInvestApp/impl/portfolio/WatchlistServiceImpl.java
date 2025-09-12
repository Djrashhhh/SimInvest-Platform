package com.example.MicroInvestApp.impl.portfolio;

import com.example.MicroInvestApp.domain.portfolio.Watchlist;
import com.example.MicroInvestApp.domain.market.SecurityStock;
import com.example.MicroInvestApp.domain.user.UserAccount;
import com.example.MicroInvestApp.dto.portfolio.RequestDTOs.CreateWatchlistRequestDTO;
import com.example.MicroInvestApp.dto.portfolio.RequestDTOs.UpdateWatchlistRequestDTO;
import com.example.MicroInvestApp.dto.portfolio.ResponseDTOs.WatchlistResponseDTO;
import com.example.MicroInvestApp.dto.portfolio.ResponseDTOs.WatchlistStatsResponseDTO;
import com.example.MicroInvestApp.dto.portfolio.ResponseDTOs.WatchlistSummaryResponseDTO;
import com.example.MicroInvestApp.dto.portfolio.ResponseDTOs.WatchlistSecurityResponseDTO;
import com.example.MicroInvestApp.exception.SecurityNotFoundException;
import com.example.MicroInvestApp.exception.portfolio.WatchlistNotFoundException;
import com.example.MicroInvestApp.exception.user.UserNotFoundException;
import com.example.MicroInvestApp.repositories.portfolio.WatchlistRepository;
import com.example.MicroInvestApp.repositories.market.SecurityStockRepository;
import com.example.MicroInvestApp.repositories.user.UserAccountRepository;
import com.example.MicroInvestApp.service.portfolio.WatchlistService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class WatchlistServiceImpl implements WatchlistService {

    private final WatchlistRepository watchlistRepository;
    private final UserAccountRepository userAccountRepository;
    private final SecurityStockRepository securityStockRepository;

    @Autowired
    public WatchlistServiceImpl(WatchlistRepository watchlistRepository,
                                UserAccountRepository userAccountRepository,
                                SecurityStockRepository securityStockRepository) {
        this.watchlistRepository = watchlistRepository;
        this.userAccountRepository = userAccountRepository;
        this.securityStockRepository = securityStockRepository;
    }

    @Override
    public WatchlistResponseDTO createWatchlist(Long userId, CreateWatchlistRequestDTO createRequest) {
        if (createRequest == null) {
            throw new IllegalArgumentException("Create request cannot be null");
        }

        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        String trimmedName = StringUtils.trimWhitespace(createRequest.getName());
        if (!StringUtils.hasText(trimmedName)) {
            throw new IllegalArgumentException("Watchlist name cannot be empty");
        }

        if (userHasWatchlistWithName(userId, trimmedName)) {
            throw new IllegalArgumentException("User already has a watchlist with name: " + trimmedName);
        }

        Watchlist watchlist = new Watchlist();
        watchlist.setUserAccount(user);
        watchlist.setName(trimmedName);
        watchlist.setDescription(StringUtils.trimWhitespace(createRequest.getDescription()));

        // Add securities if provided
        if (createRequest.getSecurityIds() != null && !createRequest.getSecurityIds().isEmpty()) {
            Set<SecurityStock> securities = getSecuritiesByIds(createRequest.getSecurityIds());
            watchlist.setSecurities(securities);
        }

        Watchlist savedWatchlist = watchlistRepository.save(watchlist);
        return convertToWatchlistResponseDTO(savedWatchlist);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<WatchlistResponseDTO> getWatchlistById(Long watchlistId, Long userId) {
        if (watchlistId == null || userId == null) {
            return Optional.empty();
        }

        return watchlistRepository.findByIdAndUserId(watchlistId, userId)
                .map(this::convertToWatchlistResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WatchlistSummaryResponseDTO> getWatchlistsContainingSymbol(String symbol, Long userId) {
        if (!StringUtils.hasText(symbol) || userId == null) {
            return new ArrayList<>();
        }

        verifyUserExists(userId);
        String upperSymbol = StringUtils.trimWhitespace(symbol).toUpperCase();

        // Find security by symbol first
        Optional<SecurityStock> securityOpt = securityStockRepository.findBySymbol(upperSymbol);
        if (securityOpt.isEmpty()) {
            return new ArrayList<>();
        }

        Long securityId = securityOpt.get().getSecurityId();
        return getWatchlistsContainingSecurity(securityId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isSymbolInUserWatchlists(Long userId, String symbol) {
        if (!StringUtils.hasText(symbol) || userId == null) {
            return false;
        }

        verifyUserExists(userId);
        String upperSymbol = StringUtils.trimWhitespace(symbol).toUpperCase();

        // Find security by symbol first
        Optional<SecurityStock> securityOpt = securityStockRepository.findBySymbol(upperSymbol);
        if (securityOpt.isEmpty()) {
            return false;
        }

        Long securityId = securityOpt.get().getSecurityId();
        List<Watchlist> userWatchlists = watchlistRepository.findByUserId(userId);

        return userWatchlists.stream()
                .anyMatch(watchlist -> {
                    Set<SecurityStock> securities = watchlist.getSecurities();
                    return securities != null && securities.stream()
                            .anyMatch(security -> security.getSecurityId().equals(securityId));
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<WatchlistSummaryResponseDTO> getUserWatchlists(Long userId) {
        verifyUserExists(userId);
        List<Watchlist> watchlists = watchlistRepository.findByUserId(userId);
        return watchlists.stream()
                .map(this::convertToWatchlistSummaryResponseDTO)
                .collect(Collectors.toList());
    }


    @Override
    public WatchlistResponseDTO updateWatchlist(Long watchlistId, Long userId, UpdateWatchlistRequestDTO updateRequest) {
        if (updateRequest == null) {
            throw new IllegalArgumentException("Update request cannot be null");
        }

        Watchlist watchlist = getWatchlistByIdAndVerifyOwnership(watchlistId, userId);

        if (StringUtils.hasText(updateRequest.getName())) {
            String trimmedName = StringUtils.trimWhitespace(updateRequest.getName());

            if (!watchlist.getName().equals(trimmedName) &&
                    userHasWatchlistWithName(userId, trimmedName)) {
                throw new IllegalArgumentException("User already has a watchlist with name: " + trimmedName);
            }
            watchlist.setName(trimmedName);
        }

        if (updateRequest.getDescription() != null) {
            watchlist.setDescription(StringUtils.trimWhitespace(updateRequest.getDescription()));
        }

        // Remove isPublic handling:
        // if (updateRequest.getIsPublic() != null) {
        //     watchlist.setPublic(updateRequest.getIsPublic());
        // }

        Watchlist updatedWatchlist = watchlistRepository.save(watchlist);
        return convertToWatchlistResponseDTO(updatedWatchlist);
    }

    @Override
    public void deleteWatchlist(Long watchlistId, Long userId) {
        getWatchlistByIdAndVerifyOwnership(watchlistId, userId);
        watchlistRepository.deleteById(watchlistId);
    }

    @Override
    public WatchlistResponseDTO addSecuritiesToWatchlist(Long watchlistId, Long userId, Set<Long> securityIds) {
        if (securityIds == null || securityIds.isEmpty()) {
            throw new IllegalArgumentException("Security IDs cannot be empty");
        }

        Watchlist watchlist = getWatchlistByIdAndVerifyOwnership(watchlistId, userId);
        Set<SecurityStock> newSecurities = getSecuritiesByIds(securityIds);

        // Add new securities to existing ones
        Set<SecurityStock> currentSecurities = watchlist.getSecurities();
        if (currentSecurities == null) {
            currentSecurities = new HashSet<>();
            watchlist.setSecurities(currentSecurities);
        }

        currentSecurities.addAll(newSecurities);

        Watchlist updatedWatchlist = watchlistRepository.save(watchlist);
        return convertToWatchlistResponseDTO(updatedWatchlist);
    }

    @Override
    public WatchlistResponseDTO removeSecuritiesFromWatchlist(Long watchlistId, Long userId, Set<Long> securityIds) {
        if (securityIds == null || securityIds.isEmpty()) {
            throw new IllegalArgumentException("Security IDs cannot be empty");
        }

        Watchlist watchlist = getWatchlistByIdAndVerifyOwnership(watchlistId, userId);

        Set<SecurityStock> securities = watchlist.getSecurities();
        if (securities != null) {
            securities.removeIf(security -> securityIds.contains(security.getSecurityId()));
        }

        Watchlist updatedWatchlist = watchlistRepository.save(watchlist);
        return convertToWatchlistResponseDTO(updatedWatchlist);
    }

    @Override
    public WatchlistResponseDTO clearWatchlist(Long watchlistId, Long userId) {
        Watchlist watchlist = getWatchlistByIdAndVerifyOwnership(watchlistId, userId);

        Set<SecurityStock> securities = watchlist.getSecurities();
        if (securities != null) {
            securities.clear();
        }

        Watchlist updatedWatchlist = watchlistRepository.save(watchlist);
        return convertToWatchlistResponseDTO(updatedWatchlist);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WatchlistSummaryResponseDTO> getWatchlistsContainingSecurity(Long securityId, Long userId) {
        verifyUserExists(userId);
        verifySecurityExists(securityId);

        List<Watchlist> watchlists = watchlistRepository.findByUserIdAndSecurityId(userId, securityId);
        return watchlists.stream()
                .map(this::convertToWatchlistSummaryResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean userHasWatchlistWithName(Long userId, String name) {
        if (!StringUtils.hasText(name)) {
            return false;
        }
        return watchlistRepository.existsByUserIdAndName(userId, StringUtils.trimWhitespace(name));
    }

    @Override
    @Transactional(readOnly = true)
    public Long getUserWatchlistCount(Long userId) {
        verifyUserExists(userId);
        return watchlistRepository.countByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean watchlistExists(Long watchlistId) {
        if (watchlistId == null) {
            return false;
        }
        return watchlistRepository.existsById(watchlistId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isWatchlistOwner(Long watchlistId, Long userId) {
        if (watchlistId == null || userId == null) {
            return false;
        }

        return watchlistRepository.findByIdAndUserId(watchlistId, userId).isPresent();
    }


    @Override
    @Transactional(readOnly = true)
    public Optional<WatchlistResponseDTO> getUserDefaultWatchlist(Long userId) {
        verifyUserExists(userId);

        return watchlistRepository.findMostRecentByUserId(userId)
                .map(this::convertToWatchlistResponseDTO);
    }


    @Override
    @Transactional(readOnly = true)
    public List<String> getWatchlistSymbols(Long watchlistId) {
        if (watchlistId == null) {
            return new ArrayList<>();
        }

        Watchlist watchlist = watchlistRepository.findById(watchlistId)
                .orElseThrow(() -> new WatchlistNotFoundException("Watchlist not found with ID: " + watchlistId));

        Set<SecurityStock> securities = watchlist.getSecurities();
        if (securities == null || securities.isEmpty()) {
            return new ArrayList<>();
        }

        return securities.stream()
                .map(SecurityStock::getSymbol)
                .filter(Objects::nonNull)
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public WatchlistResponseDTO addSecurityBySymbol(Long watchlistId, Long userId, String symbol) {
        if (!StringUtils.hasText(symbol)) {
            throw new IllegalArgumentException("Symbol cannot be empty");
        }

        String upperSymbol = StringUtils.trimWhitespace(symbol).toUpperCase();

        // Find security by symbol
        SecurityStock security = securityStockRepository.findBySymbol(upperSymbol)
                .orElseThrow(() -> new SecurityNotFoundException("Security not found with symbol: " + upperSymbol));

        // Add to watchlist using existing method
        return addSecuritiesToWatchlist(watchlistId, userId, Set.of(security.getSecurityId()));
    }

    @Override
    public WatchlistResponseDTO removeSecurityBySymbol(Long watchlistId, Long userId, String symbol) {
        if (!StringUtils.hasText(symbol)) {
            throw new IllegalArgumentException("Symbol cannot be empty");
        }

        String upperSymbol = StringUtils.trimWhitespace(symbol).toUpperCase();

        // Find security by symbol
        SecurityStock security = securityStockRepository.findBySymbol(upperSymbol)
                .orElseThrow(() -> new SecurityNotFoundException("Security not found with symbol: " + upperSymbol));

        // Remove from watchlist using existing method
        return removeSecuritiesFromWatchlist(watchlistId, userId, Set.of(security.getSecurityId()));
    }

    @Override
    @Transactional(readOnly = true)
    public WatchlistStatsResponseDTO getUserWatchlistStats(Long userId) {
        verifyUserExists(userId);

        long totalWatchlists = watchlistRepository.countByUserId(userId);
        Set<Long> uniqueSecurityIds = watchlistRepository.findSecurityIdsByUserId(userId);
        long totalSecurities = uniqueSecurityIds.size();

        WatchlistStatsResponseDTO stats = new WatchlistStatsResponseDTO();
        stats.setTotalWatchlists(totalWatchlists);
        stats.setTotalSecurities(totalSecurities);

        return stats;
    }

    // Private helper methods
    private Watchlist getWatchlistByIdAndVerifyOwnership(Long watchlistId, Long userId) {
        if (watchlistId == null) {
            throw new IllegalArgumentException("Watchlist ID cannot be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        Watchlist watchlist = watchlistRepository.findById(watchlistId)
                .orElseThrow(() -> new WatchlistNotFoundException("Watchlist not found with ID: " + watchlistId));

        if (!watchlist.getUserAccount().getUserId().equals(userId)) {
            throw new WatchlistNotFoundException("Access denied: You don't own this watchlist");
        }

        return watchlist;
    }

    private Set<SecurityStock> getSecuritiesByIds(Set<Long> securityIds) {
        if (securityIds == null || securityIds.isEmpty()) {
            return new HashSet<>();
        }

        // Remove any null values
        Set<Long> validIds = securityIds.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (validIds.isEmpty()) {
            return new HashSet<>();
        }

        List<SecurityStock> securities = securityStockRepository.findAllById(validIds);

        if (securities.size() != validIds.size()) {
            Set<Long> foundIds = securities.stream()
                    .map(SecurityStock::getSecurityId)
                    .collect(Collectors.toSet());
            Set<Long> missingIds = new HashSet<>(validIds);
            missingIds.removeAll(foundIds);
            throw new SecurityNotFoundException("Securities not found with IDs: " + missingIds);
        }

        return new HashSet<>(securities);
    }

    private void verifyUserExists(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (!userAccountRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found with ID: " + userId);
        }
    }

    private void verifySecurityExists(Long securityId) {
        if (securityId == null) {
            throw new IllegalArgumentException("Security ID cannot be null");
        }
        if (!securityStockRepository.existsById(securityId)) {
            throw new SecurityNotFoundException("Security not found with ID: " + securityId);
        }
    }

    private WatchlistResponseDTO convertToWatchlistResponseDTO(Watchlist watchlist) {
        if (watchlist == null) {
            return null;
        }

        WatchlistResponseDTO dto = new WatchlistResponseDTO();
        dto.setWatchlistId(watchlist.getWatchlistId());
        dto.setUserId(watchlist.getUserAccount().getUserId());
        dto.setName(watchlist.getName());
        dto.setDescription(watchlist.getDescription());
        dto.setCreatedAt(watchlist.getCreatedAt());
        dto.setUpdatedAt(watchlist.getUpdatedAt());

        Set<SecurityStock> securities = watchlist.getSecurities();
        if (securities != null && !securities.isEmpty()) {
            dto.setSecurityCount(securities.size());
            List<WatchlistSecurityResponseDTO> securityDTOs = securities.stream()
                    .map(this::convertToWatchlistSecurityResponseDTO)
                    .sorted(Comparator.comparing(WatchlistSecurityResponseDTO::getSymbol))
                    .collect(Collectors.toList());
            dto.setSecurities(securityDTOs);
        } else {
            dto.setSecurityCount(0);
            dto.setSecurities(new ArrayList<>());
        }

        return dto;
    }


    private WatchlistSummaryResponseDTO convertToWatchlistSummaryResponseDTO(Watchlist watchlist) {
        if (watchlist == null) {
            return null;
        }

        Set<SecurityStock> securities = watchlist.getSecurities();
        int securityCount = securities != null ? securities.size() : 0;

        return new WatchlistSummaryResponseDTO(
                watchlist.getWatchlistId(),
                watchlist.getName(),
                watchlist.getDescription(),
                securityCount,
                watchlist.getUpdatedAt()
        );
    }

    private WatchlistSecurityResponseDTO convertToWatchlistSecurityResponseDTO(SecurityStock security) {
        if (security == null) {
            return null;
        }

        WatchlistSecurityResponseDTO dto = new WatchlistSecurityResponseDTO();
        dto.setSecurityId(security.getSecurityId());
        dto.setSymbol(security.getSymbol());
        dto.setCompanyName(security.getCompanyName());
        dto.setCurrentPrice(security.getCurrentPrice());
        dto.setLastUpdated(security.getUpdatedDate());

        return dto;
    }
}