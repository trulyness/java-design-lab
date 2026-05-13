package com.trading.exceptions;

public class OrderBookNotFoundException extends RuntimeException {
    public OrderBookNotFoundException() {
        super("OrderBook not found");
    }
}
