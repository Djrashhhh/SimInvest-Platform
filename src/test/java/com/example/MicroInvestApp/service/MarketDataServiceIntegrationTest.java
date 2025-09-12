package com.example.MicroInvestApp.service;

import com.example.MicroInvestApp.domain.enums.Exchange;
import com.example.MicroInvestApp.domain.enums.SecuritySector;
import com.example.MicroInvestApp.domain.enums.SecurityType;
import com.example.MicroInvestApp.domain.market.MarketData;
import com.example.MicroInvestApp.domain.market.PriceHistory;
import com.example.MicroInvestApp.domain.market.SecurityStock;
import com.example.MicroInvestApp.repositories.market.SecurityStockRepository;
import com.example.MicroInvestApp.service.market.MarketDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class MarketDataServiceIntegrationTest {

    @Autowired
    private MarketDataService marketDataService;

    @Autowired
    private SecurityStockRepository securityStockRepository;

    private SecurityStock testSecurity;

    @BeforeEach
    void setUp() {
        // Create a test security
        testSecurity = new SecurityStock(
                "AAPL",
                "Apple Inc.",
                SecuritySector.TECHNOLOGY,
                new BigDecimal("3000000000000"), // 3T market cap
                new BigDecimal("150.00"),
                SecurityType.STOCK,
                Exchange.NASDAQ
        );
        testSecurity.setActive(true);
        testSecurity = securityStockRepository.save(testSecurity);
    }

    @Test
    void testGetMarketDataNotFound() {
        // Test retrieving market data that doesn't exist
        Optional<MarketData> result = marketDataService.getMarketData("AAPL", LocalDate.now());
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetLatestMarketDataNotFound() {
        // Test retrieving latest market data that doesn't exist
        Optional<MarketData> result = marketDataService.getLatestMarketData("AAPL");
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetHistoricalPricesEmpty() {
        // Test retrieving historical prices when none exist
        LocalDate from = LocalDate.now().minusDays(7);
        LocalDate to = LocalDate.now().minusDays(1);

        List<PriceHistory> result = marketDataService.getHistoricalPrices("AAPL", from, to);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testSecurityExists() {
        // Test that our test security was created successfully
        Optional<SecurityStock> security = securityStockRepository.findBySymbol("AAPL");
        assertTrue(security.isPresent());
        assertEquals("Apple Inc.", security.get().getCompanyName());
        assertEquals(SecuritySector.TECHNOLOGY, security.get().getSector());
    }

    // Commented out tests that require actual API calls
    @Test
    void testFetchCurrentMarketData() {
        // This test requires a valid Finnhub API key
        // Uncomment and modify when you have API access configured
        /*
        try {
            MarketData marketData = marketDataService.fetchAndStoreCurrentMarketData("AAPL");

            assertNotNull(marketData);
            assertNotNull(marketData.getClosePrice());
            assertTrue(marketData.getClosePrice().compareTo(BigDecimal.ZERO) > 0);
            assertEquals(LocalDate.now(), marketData.getMarketDate());
            assertEquals("Finnhub", marketData.getDataSource());
        } catch (RuntimeException e) {
            // Expected if no API key is configured
            assertTrue(e.getMessage().contains("API") || e.getMessage().contains("key"));
        }
        */
    }

    @Test
    void testFetchHistoricalMarketData() {
        // This test requires a valid Finnhub API key
        // Uncomment and modify when you have API access configured
        /*
        try {
            LocalDate from = LocalDate.now().minusDays(7);
            LocalDate to = LocalDate.now().minusDays(1);

            List<MarketData> historicalData = marketDataService
                    .fetchAndStoreHistoricalMarketData("AAPL", from, to);

            assertNotNull(historicalData);
            assertFalse(historicalData.isEmpty());

            for (MarketData data : historicalData) {
                assertNotNull(data.getOpenPrice());
                assertNotNull(data.getClosePrice());
                assertNotNull(data.getHighPrice());
                assertNotNull(data.getLowPrice());
                assertTrue(data.getVolume() >= 0);
            }
        } catch (RuntimeException e) {
            // Expected if no API key is configured
            assertTrue(e.getMessage().contains("API") || e.getMessage().contains("key"));
        }
        */
    }
}