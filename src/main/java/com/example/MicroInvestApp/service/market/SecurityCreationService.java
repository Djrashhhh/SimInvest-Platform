// SecurityCreationService.java - Complete fixed version with proper sector mapping
package com.example.MicroInvestApp.service.market;

import com.example.MicroInvestApp.domain.enums.Exchange;
import com.example.MicroInvestApp.domain.enums.SecuritySector;
import com.example.MicroInvestApp.domain.enums.SecurityType;
import com.example.MicroInvestApp.domain.market.SecurityStock;
import com.example.MicroInvestApp.dto.finnhub.FinnhubCompanyProfileDTO;
import com.example.MicroInvestApp.dto.finnhub.FinnhubQuoteDTO;
import com.example.MicroInvestApp.repositories.market.SecurityStockRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.time.Instant;
import java.math.BigDecimal;
import java.util.Optional;

@Service
@Transactional
public class SecurityCreationService {

    private static final Logger logger = LoggerFactory.getLogger(SecurityCreationService.class);

    private final FinnhubClientService finnhubClient;
    private final SecurityStockRepository securityStockRepository;

    @Autowired
    public SecurityCreationService(FinnhubClientService finnhubClient,
                                   SecurityStockRepository securityStockRepository) {
        this.finnhubClient = finnhubClient;
        this.securityStockRepository = securityStockRepository;
    }

    /**
     * Creates a new SecurityStock from Finnhub data if the symbol is valid
     * @param symbol Stock symbol to create
     * @return Created SecurityStock or throws exception if invalid
     */
    public SecurityStock createSecurityFromSymbol(String symbol) {
        logger.info("Attempting to create security for symbol: {}", symbol);

        try {
            // Fetch quote and company profile
            FinnhubQuoteDTO quote = finnhubClient.getQuote(symbol).block();
            FinnhubCompanyProfileDTO profile = finnhubClient.getCompanyProfile(symbol).block();

            if (quote == null || quote.getCurrentPrice() == null) {
                throw new RuntimeException("No quote data available for symbol: " + symbol);
            }

            logger.info("Successfully fetched data for symbol: {}", symbol);
            if (profile != null) {
                logger.info("Company profile data: name={}, sector={}, industry={}, exchange={}",
                        profile.getCompanyName(), profile.getSector(), profile.getIndustry(), profile.getExchange());
            }

            // Create new security stock
            SecurityStock security = new SecurityStock();
            security.setSymbol(symbol);

            // Set current price from quote
            security.setCurrentPrice(quote.getCurrentPrice());

            // Set company name
            if (profile != null && profile.getCompanyName() != null && !profile.getCompanyName().trim().isEmpty()) {
                security.setCompanyName(profile.getCompanyName());
                logger.info("Using company name from profile: {}", profile.getCompanyName());
            } else {
                security.setCompanyName(symbol); // Fallback to symbol
                logger.warn("No company name available, using symbol: {}", symbol);
            }

            // Set market cap
            if (profile != null && profile.getMarketCap() != null && profile.getMarketCap().compareTo(BigDecimal.ZERO) > 0) {
                try {
                    BigDecimal rawMarketCap = profile.getMarketCap();
                    logger.debug("Raw market cap from Finnhub for {}: {}", symbol, profile.getMarketCap());

                    BigDecimal roundedMarketCap = rawMarketCap.setScale(2, RoundingMode.HALF_UP);
                    logger.debug("Setting market cap for symbol {}: {}", symbol, roundedMarketCap);
                    security.setMarketCap(roundedMarketCap);
                } catch (ArithmeticException e) {
                    logger.error("Invalid market cap format for symbol {}: {}", symbol, profile.getMarketCap());
                    security.setMarketCap(new BigDecimal("1.00")); // safe fallback
                }
            } else {
                logger.warn("Missing or invalid market cap for symbol {}, defaulting to 1.00", symbol);
                security.setMarketCap(new BigDecimal("1.00"));
            }

            // Set other fields with proper defaults
            security.setSecurityType(SecurityType.STOCK);
            security.setActive(true);
            security.setCreatedDate(Instant.now());
            security.setUpdatedDate(Instant.now());

            // Handle sector - FIXED: Use both sector and industry fields for better mapping
            SecuritySector mappedSector = mapSectorFromFinnhub(profile);
            security.setSector(mappedSector);
            logger.info("Mapped sector for {}: {}", symbol, mappedSector);

            // Handle exchange
            if (profile != null && profile.getExchange() != null && !profile.getExchange().trim().isEmpty()) {
                security.setExchange(mapExchangeFromFinnhub(profile));
            } else {
                security.setExchange(Exchange.NASDAQ); // Default
                logger.warn("No exchange information available, defaulting to NASDAQ");
            }

            // Save the security
            security = securityStockRepository.save(security);

            logger.info("Successfully created security: {} - {} (${}) [{}] Sector: {}",
                    security.getSymbol(),
                    security.getCompanyName(),
                    security.getCurrentPrice(),
                    security.getExchange(),
                    security.getSector());

            return security;

        } catch (Exception e) {
            logger.error("Failed to create security for symbol {}: {}", symbol, e.getMessage());
            throw new RuntimeException("Unable to create security for symbol: " + symbol, e);
        }
    }

    /**
     * FIXED: Maps Finnhub sector/industry information to our SecuritySector enum
     * Uses both gicsSector and finnhubIndustry fields for comprehensive mapping
     */
    private SecuritySector mapSectorFromFinnhub(FinnhubCompanyProfileDTO profile) {
        if (profile == null) {
            logger.warn("No profile information available, defaulting to CONSUMER_DISCRETIONARY");
            return SecuritySector.TECHNOLOGY; // Changed default from TECHNOLOGY
        }

        // Try to get sector information from both fields
        String gicsSector = profile.getSector(); // This comes from gicsSector in JSON
        String finnhubIndustry = profile.getIndustry(); // This comes from finnhubIndustry in JSON
        String subIndustry = profile.getSubIndustry(); // Also check sub-industry
        String companyName = profile.getCompanyName(); // Use company name as last resort

        logger.info("SECTOR MAPPING DEBUG - gicsSector: '{}', finnhubIndustry: '{}', subIndustry: '{}', companyName: '{}'",
                gicsSector, finnhubIndustry, subIndustry, companyName);

        // First try to map using GICS sector (more standardized)
        if (gicsSector != null && !gicsSector.trim().isEmpty()) {
            SecuritySector mappedSector = mapGicsSector(gicsSector);
            if (mappedSector != null) {
                logger.info("Successfully mapped using GICS sector '{}' -> {}", gicsSector, mappedSector);
                return mappedSector;
            }
        }

        // If GICS sector mapping fails, try Finnhub industry
        if (finnhubIndustry != null && !finnhubIndustry.trim().isEmpty()) {
            SecuritySector mappedSector = mapFinnhubIndustry(finnhubIndustry);
            if (mappedSector != null) {
                logger.info("Successfully mapped using Finnhub industry '{}' -> {}", finnhubIndustry, mappedSector);
                return mappedSector;
            }
        }

        // Try sub-industry as fallback
        if (subIndustry != null && !subIndustry.trim().isEmpty()) {
            SecuritySector mappedSector = mapFinnhubIndustry(subIndustry); // Reuse the same logic
            if (mappedSector != null) {
                logger.info("Successfully mapped using sub-industry '{}' -> {}", subIndustry, mappedSector);
                return mappedSector;
            }
        }

        // Last resort: try to guess from company name
        if (companyName != null && !companyName.trim().isEmpty()) {
            SecuritySector mappedSector = mapFromCompanyName(companyName);
            if (mappedSector != null) {
                logger.info("Successfully mapped using company name '{}' -> {}", companyName, mappedSector);
                return mappedSector;
            }
        }

        logger.warn("Could not map sector from any available data - gicsSector='{}', finnhubIndustry='{}', subIndustry='{}', companyName='{}', defaulting to CONSUMER_DISCRETIONARY",
                gicsSector, finnhubIndustry, subIndustry, companyName);
        return SecuritySector.CONSUMER_DISCRETIONARY; // Changed default from TECHNOLOGY
    }

    /**
     * Maps GICS Sector to our SecuritySector enum
     * GICS sectors are standardized: https://www.msci.com/our-solutions/indexes/gics
     */
    private SecuritySector mapGicsSector(String gicsSector) {
        if (gicsSector == null) return null;

        String sector = gicsSector.toLowerCase().trim();
        logger.debug("Mapping GICS sector: '{}'", sector);

        // Standard GICS sectors
        if (sector.contains("information technology") || sector.contains("technology")) {
            return SecuritySector.TECHNOLOGY;
        } else if (sector.contains("health care") || sector.contains("healthcare")) {
            return SecuritySector.HEALTHCARE;
        } else if (sector.contains("financials") || sector.contains("financial")) {
            return SecuritySector.FINANCIALS;
        } else if (sector.contains("consumer discretionary")) {
            return SecuritySector.CONSUMER_DISCRETIONARY;
        } else if (sector.contains("consumer staples")) {
            return SecuritySector.CONSUMER_STAPLES;
        } else if (sector.contains("communication services") || sector.contains("communication")) {
            return SecuritySector.COMMUNICATION_SERVICES;
        } else if (sector.contains("industrials") || sector.contains("industrial")) {
            return SecuritySector.INDUSTRIALS;
        } else if (sector.contains("energy")) {
            return SecuritySector.ENERGY;
        } else if (sector.contains("materials")) {
            return SecuritySector.MATERIALS;
        } else if (sector.contains("utilities")) {
            return SecuritySector.UTILITIES;
        } else if (sector.contains("real estate")) {
            return SecuritySector.REAL_ESTATE;
        }

        return null; // No mapping found
    }

    /**
     * Maps Finnhub Industry to our SecuritySector enum
     * This is a fallback when GICS sector is not available
     */
    private SecuritySector mapFinnhubIndustry(String finnhubIndustry) {
        if (finnhubIndustry == null) return null;

        String industry = finnhubIndustry.toLowerCase().trim();
        logger.debug("Mapping Finnhub industry: '{}'", industry);

        // Technology related
        if (industry.contains("software") || industry.contains("technology") ||
                industry.contains("internet") || industry.contains("semiconductor") ||
                industry.contains("computer") || industry.contains("electronics")) {
            return SecuritySector.TECHNOLOGY;
        }

        // Healthcare related
        else if (industry.contains("pharmaceutical") || industry.contains("biotech") ||
                industry.contains("medical") || industry.contains("health")) {
            return SecuritySector.HEALTHCARE;
        }

        // Financial related
        else if (industry.contains("bank") || industry.contains("insurance") ||
                industry.contains("financial") || industry.contains("investment")) {
            return SecuritySector.FINANCIALS;
        }

        // Retail and Consumer Discretionary
        else if (industry.contains("retail") || industry.contains("restaurant") ||
                industry.contains("automotive") || industry.contains("hotel") ||
                industry.contains("entertainment") || industry.contains("media") ||
                industry.contains("apparel") || industry.contains("consumer discretionary")) {
            return SecuritySector.CONSUMER_DISCRETIONARY;
        }

        // Consumer Staples (essentials like food, household products)
        else if (industry.contains("food") || industry.contains("beverage") ||
                industry.contains("tobacco") || industry.contains("household") ||
                industry.contains("personal products") || industry.contains("consumer staples") ||
                industry.contains("grocery") || industry.contains("supermarket") ||
                industry.contains("discount store") || industry.contains("hypermarket")) {
            return SecuritySector.CONSUMER_STAPLES;
        }

        // Communication Services
        else if (industry.contains("telecom") || industry.contains("wireless") ||
                industry.contains("broadcasting") || industry.contains("publishing")) {
            return SecuritySector.COMMUNICATION_SERVICES;
        }

        // Industrials
        else if (industry.contains("aerospace") || industry.contains("defense") ||
                industry.contains("construction") || industry.contains("machinery") ||
                industry.contains("transportation") || industry.contains("logistics")) {
            return SecuritySector.INDUSTRIALS;
        }

        // Energy
        else if (industry.contains("oil") || industry.contains("gas") ||
                industry.contains("energy") || industry.contains("petroleum")) {
            return SecuritySector.ENERGY;
        }

        // Materials
        else if (industry.contains("mining") || industry.contains("chemical") ||
                industry.contains("steel") || industry.contains("aluminum") ||
                industry.contains("copper") || industry.contains("materials")) {
            return SecuritySector.MATERIALS;
        }

        // Utilities
        else if (industry.contains("electric") || industry.contains("utility") ||
                industry.contains("water") || industry.contains("gas utility")) {
            return SecuritySector.UTILITIES;
        }

        // Real Estate
        else if (industry.contains("real estate") || industry.contains("reit")) {
            return SecuritySector.REAL_ESTATE;
        }

        return null; // No mapping found
    }

    /**
     * Maps company name to sector as last resort
     * Useful when Finnhub doesn't provide sector/industry data
     */
    private SecuritySector mapFromCompanyName(String companyName) {
        if (companyName == null) return null;

        String name = companyName.toLowerCase().trim();
        logger.debug("Mapping from company name: '{}'", name);

        // Well-known companies by sector
        if (name.contains("walmart") || name.contains("target") || name.contains("costco") ||
                name.contains("kroger") || name.contains("home depot") || name.contains("lowes")) {
            return SecuritySector.CONSUMER_DISCRETIONARY;
        } else if (name.contains("procter") || name.contains("coca-cola") || name.contains("pepsi") ||
                name.contains("unilever") || name.contains("nestlé") || name.contains("general mills")) {
            return SecuritySector.CONSUMER_STAPLES;
        } else if (name.contains("apple") || name.contains("microsoft") || name.contains("google") ||
                name.contains("amazon") || name.contains("meta") || name.contains("netflix")) {
            return SecuritySector.TECHNOLOGY;
        } else if ((name.contains("johnson") && name.contains("johnson")) || name.contains("pfizer") ||
                name.contains("merck") || name.contains("abbott")) {
            return SecuritySector.HEALTHCARE;
        } else if (name.contains("jpmorgan") || name.contains("bank of america") || name.contains("wells fargo") ||
                name.contains("goldman sachs") || name.contains("morgan stanley")) {
            return SecuritySector.FINANCIALS;
        } else if (name.contains("exxon") || name.contains("chevron") || name.contains("conocophillips")) {
            return SecuritySector.ENERGY;
        }

        return null; // No mapping found
    }

    /**
     * Maps Finnhub exchange string to our Exchange enum
     * Uses common exchange abbreviations and names
     */
    private Exchange mapExchangeFromFinnhub(FinnhubCompanyProfileDTO profile) {
        if (profile == null || profile.getExchange() == null) {
            logger.warn("No exchange information available, defaulting to NASDAQ");
            return Exchange.NASDAQ; // Default fallback for US stocks
        }

        String exchange = profile.getExchange().toUpperCase().trim();
        logger.debug("Mapping exchange: '{}'", exchange);

        // Map common exchange names/codes to our enum values
        if (exchange.contains("NASDAQ")) {
            return Exchange.NASDAQ;
        } else if (exchange.contains("NYSE") || exchange.contains("NEW YORK")) {
            return Exchange.NYSE;
        } else if (exchange.contains("TSX") || exchange.contains("TORONTO")) {
            return Exchange.TSX;
        } else if (exchange.contains("LSE") || exchange.contains("LONDON")) {
            return Exchange.LSE;
        } else if (exchange.contains("HKEX") || exchange.contains("HONG KONG")) {
            return Exchange.HKEX;
        } else if (exchange.contains("JPX") || exchange.contains("TOKYO") || exchange.equals("TSE")) {
            return Exchange.JPX;
        } else if (exchange.contains("SSE") || exchange.contains("SHANGHAI")) {
            return Exchange.SSE;
        } else if (exchange.contains("SZSE") || exchange.contains("SHENZHEN")) {
            return Exchange.SZSE;
        } else {
            logger.warn("Unknown exchange '{}', defaulting to NASDAQ", exchange);
            return Exchange.NASDAQ; // Default fallback
        }
    }

    /**
     * Checks if a security already exists in the database
     * @param symbol Stock symbol to check
     * @return Optional containing the security if found
     */
    public Optional<SecurityStock> findExistingSecurity(String symbol) {
        return securityStockRepository.findBySymbol(symbol.toUpperCase());
    }

    /**
     * NEW METHOD: Updates the sector information for an existing security
     * Useful for correcting securities that were previously assigned incorrect sectors
     * @param security Existing security to update
     * @return Updated security with corrected sector
     */
    public SecurityStock updateSecuritySector(SecurityStock security) {
        logger.info("Attempting to update sector for existing security: {}", security.getSymbol());

        try {
            // Fetch fresh company profile data
            FinnhubCompanyProfileDTO profile = finnhubClient.getCompanyProfile(security.getSymbol()).block();

            if (profile != null) {
                logger.info("Fetched fresh profile data for existing security: {}", security.getSymbol());
                logger.info("Company profile data: name={}, sector={}, industry={}, exchange={}",
                        profile.getCompanyName(), profile.getSector(), profile.getIndustry(), profile.getExchange());

                // Try to get a better sector mapping
                SecuritySector newSector = mapSectorFromFinnhub(profile);

                // Update the sector and timestamp
                security.setSector(newSector);
                security.setUpdatedDate(Instant.now());

                // Save the updated security
                security = securityStockRepository.save(security);

                logger.info("Successfully updated sector for security {} to {}",
                        security.getSymbol(), security.getSector());

                return security;
            } else {
                logger.warn("No profile data available for sector update of {}", security.getSymbol());
                return security; // Return unchanged if no profile data
            }

        } catch (Exception e) {
            logger.error("Failed to update sector for security {}: {}", security.getSymbol(), e.getMessage());
            return security; // Return unchanged if update fails
        }
    }
}