// Enhanced FinnhubClientService.java - Added company profile fetching capability
package com.example.MicroInvestApp.service.market;

import com.example.MicroInvestApp.config.FinnhubConfig;
import com.example.MicroInvestApp.dto.finnhub.FinnhubCandleDTO;
import com.example.MicroInvestApp.dto.finnhub.FinnhubCompanyProfileDTO;
import com.example.MicroInvestApp.dto.finnhub.FinnhubQuoteDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.math.BigDecimal;
import java.net.URI;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneOffset;

@Service
public class FinnhubClientService {

    private static final Logger logger = LoggerFactory.getLogger(FinnhubClientService.class);

    private final WebClient webClient;
    private final FinnhubConfig finnhubConfig;

    @Autowired
    public FinnhubClientService(FinnhubConfig finnhubConfig) {
        this.finnhubConfig = finnhubConfig;
        logger.info("Finnhub API Key (first 5 chars): {}",
                finnhubConfig.getApiKey() != null ?
                        finnhubConfig.getApiKey().substring(0, Math.min(5, finnhubConfig.getApiKey().length())) + "..." :
                        "NULL");
        this.webClient = WebClient.builder()
                .baseUrl(finnhubConfig.getBaseUrl())
                .build();
    }

    /**
     * Fetches real-time quote data for a given symbol
     * @param symbol Stock symbol (e.g., "AAPL")
     * @return FinnhubQuoteDTO containing current price data
     */
    public Mono<FinnhubQuoteDTO> getQuote(String symbol) {
        logger.info("Making API call with key ending in: {}",
                finnhubConfig.getApiKey().substring(finnhubConfig.getApiKey().length() - 4));

        return webClient.get()
                .uri(uriBuilder -> {
                    URI uri = uriBuilder
                            .path("/quote")
                            .queryParam("symbol", symbol)
                            .queryParam("token", finnhubConfig.getApiKey())
                            .build();
                    logger.info("Calling Finnhub URL: {}", uri.toString().replaceAll("token=[^&]+", "token=***"));
                    return uri;
                })
                .retrieve()
                .bodyToMono(FinnhubQuoteDTO.class)
                .timeout(Duration.ofSeconds(finnhubConfig.getTimeoutSeconds()))
                .retryWhen(Retry.backoff(finnhubConfig.getRetryAttempts(), Duration.ofSeconds(1)))
                .doOnSuccess(quote -> logger.info("Successfully fetched quote for {}: ${}",
                        symbol, quote != null ? quote.getCurrentPrice() : "null"))
                .doOnError(error -> logger.error("Error fetching quote for {}: {}",
                        symbol, error.getMessage()));
    }

    /**
     * NEW METHOD: Fetches company profile information for auto-creating securities
     * @param symbol Stock symbol (e.g., "AAPL")
     * @return FinnhubCompanyProfileDTO containing company information
     */
    public Mono<FinnhubCompanyProfileDTO> getCompanyProfile(String symbol) {
        logger.info("Fetching company profile for symbol: {}", symbol);

        return webClient.get()
                .uri(uriBuilder -> {
                    URI uri = uriBuilder
                            .path("/stock/profile2")
                            .queryParam("symbol", symbol)
                            .queryParam("token", finnhubConfig.getApiKey())
                            .build();
                    logger.debug("Calling Finnhub company profile URL: {}",
                            uri.toString().replaceAll("token=[^&]+", "token=***"));
                    return uri;
                })
                .retrieve()
                .bodyToMono(FinnhubCompanyProfileDTO.class)
                .timeout(Duration.ofSeconds(finnhubConfig.getTimeoutSeconds()))
                .retryWhen(Retry.backoff(finnhubConfig.getRetryAttempts(), Duration.ofSeconds(1)))
                .doOnSuccess(profile -> logger.info("Successfully fetched company profile for {}: {}",
                        symbol, profile != null ? profile.getCompanyName() : "null"))
                .doOnError(error -> logger.error("Error fetching company profile for {}: {}",
                        symbol, error.getMessage()));
    }

    /**
     * NEW METHOD: Validates if a symbol exists and has valid quote data
     * This is used to verify symbols before auto-creating securities
     * @param symbol Stock symbol to validate
     * @return true if symbol is valid and tradeable, false otherwise
     */
    public Mono<Boolean> validateSymbol(String symbol) {
        logger.debug("Validating symbol: {}", symbol);

        return getQuote(symbol)
                .map(quote -> {
                    // Symbol is valid if we get a quote with a positive current price
                    boolean isValid = quote != null &&
                            quote.getCurrentPrice() != null &&
                            quote.getCurrentPrice().doubleValue() > 0;
                    logger.debug("Symbol {} validation result: {}", symbol, isValid);
                    return isValid;
                })
                .onErrorReturn(false) // If any error occurs, consider symbol invalid
                .doOnNext(isValid -> logger.debug("Symbol {} is {}", symbol, isValid ? "valid" : "invalid"));
    }

    /**
     * Fetches historical candle data for a given symbol and date range
     * @param symbol Stock symbol
     * @param from Start date
     * @param to End date
     * @param resolution Resolution (D for daily, W for weekly, M for monthly)
     * @return FinnhubCandleDTO containing historical price data
     */
    public Mono<FinnhubCandleDTO> getCandles(String symbol, LocalDate from, LocalDate to, String resolution) {
        logger.info("Fetching candle data for symbol: {} from {} to {} with resolution {}",
                symbol, from, to, resolution);

        long fromTimestamp = from.atStartOfDay(ZoneOffset.UTC).toEpochSecond();
        long toTimestamp = to.atStartOfDay(ZoneOffset.UTC).toEpochSecond();

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/stock/candle")
                        .queryParam("symbol", symbol)
                        .queryParam("resolution", resolution)
                        .queryParam("from", fromTimestamp)
                        .queryParam("to", toTimestamp)
                        .queryParam("token", finnhubConfig.getApiKey())
                        .build())
                .retrieve()
                .bodyToMono(FinnhubCandleDTO.class)
                .timeout(Duration.ofSeconds(finnhubConfig.getTimeoutSeconds()))
                .retryWhen(Retry.backoff(finnhubConfig.getRetryAttempts(), Duration.ofSeconds(1)))
                .doOnSuccess(candles -> logger.info("Successfully fetched {} candles for {}",
                        candles != null && candles.getClosePrices() != null ? candles.getClosePrices().size() : 0, symbol))
                .doOnError(error -> logger.error("Error fetching candles for {}: {}",
                        symbol, error.getMessage()));
    }

    /**
     * Get the previous trading day's closing price for a symbol
     * Uses yesterday's candle data to get actual closing price
     */
    /**
     * Get the previous closing price from today's quote (much simpler than historical data)
     * Finnhub includes previous close in the real-time quote response
     */
    public Mono<BigDecimal> getPreviousClosingPrice(String symbol) {
        logger.debug("Getting previous closing price from quote for symbol: {}", symbol);

        return getQuote(symbol)
                .map(quote -> {
                    if (quote != null && quote.getPreviousClose() != null) {
                        logger.debug("Previous closing price for {} from quote: ${}", symbol, quote.getPreviousClose());
                        return quote.getPreviousClose();
                    }
                    return null;
                })
                .doOnError(error -> logger.error("Error getting previous closing price for {}: {}",
                        symbol, error.getMessage()));
    }

    private LocalDate getPreviousMarketDay(LocalDate date) {
        LocalDate previousDay = date.minusDays(1);
        while (!isMarketDay(previousDay)) {
            previousDay = previousDay.minusDays(1);
        }
        return previousDay;
    }

    private boolean isMarketDay(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
    }

    /**
     * Handle API errors and rate limiting
     */
    private <T> Mono<T> handleApiErrors(Mono<T> request) {
        return request.onErrorResume(WebClientResponseException.class, ex -> {
            if (ex.getStatusCode().value() == 429) {
                logger.warn("Rate limit exceeded, waiting before retry");
                return Mono.delay(Duration.ofSeconds(5)).then(Mono.error(ex));
            }
            logger.error("API request failed with status: {} and message: {}",
                    ex.getStatusCode(), ex.getMessage());
            return Mono.error(ex);
        });
    }
}