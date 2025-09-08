package com.example.MicroInvestApp.exception.Orders;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class OrderExecutionException extends RuntimeException {
    private final String orderType;
    private final String symbol;
    public OrderExecutionException(String message, String orderType, String symbol, Throwable cause) {
        this.orderType= orderType;
        this.symbol = symbol;
    }

    // Getters
    public String getOrderType() {
        return orderType;
    }
    public String getSymbol() {
        return symbol;
    }

}
