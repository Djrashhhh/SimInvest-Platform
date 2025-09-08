package com.example.MicroInvestApp.controller;

import com.example.MicroInvestApp.domain.enums.Exchange;
import com.example.MicroInvestApp.domain.enums.SecuritySector;
import com.example.MicroInvestApp.domain.enums.SecurityType;
import com.example.MicroInvestApp.domain.market.SecurityStock;
import com.example.MicroInvestApp.repositories.market.SecurityStockRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class MarketDataControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SecurityStockRepository securityStockRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private SecurityStock testSecurity;

    @BeforeEach
    void setUp() {
        testSecurity = new SecurityStock(
                "AAPL",
                "Apple Inc.",
                SecuritySector.TECHNOLOGY,
                new BigDecimal("3000000000000"),
                new BigDecimal("150.00"),
                SecurityType.STOCK,
                Exchange.NASDAQ
        );
        testSecurity.setActive(true);
        testSecurity = securityStockRepository.save(testSecurity);
    }

    @Test
    void testGetLatestMarketDataNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/market-data/latest/NONEXISTENT")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetHistoricalPricesWithInvalidDateRange() throws Exception {
        mockMvc.perform(get("/api/v1/market-data/history/AAPL")
                        .param("from", "2024-01-01")
                        .param("to", "2024-01-31")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid date range"));
    }

    @Test
    void testGetHistoricalPricesValidRequest() throws Exception {
        mockMvc.perform(get("/api/v1/market-data/history/AAPL")
                        .param("from", "2024-01-01")
                        .param("to", "2024-01-31")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void testGetMarketDataNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/market-data/AAPL")
                        .param("date", "2024-01-15")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateCurrentPriceSecurityNotFound() throws Exception {
        mockMvc.perform(put("/api/v1/market-data/price/update/NONEXISTENT")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").exists());
    }

    // Note: Tests for API endpoints that actually call Finnhub should be mocked
    // or run only with valid API keys in integration environment
}