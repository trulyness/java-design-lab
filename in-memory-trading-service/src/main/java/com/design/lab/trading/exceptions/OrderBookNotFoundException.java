package com.design.lab.trading.exceptions;

public class OrderBookNotFoundException extends RuntimeException {
    public OrderBookNotFoundException() {
        super("OrderBook not found");
    }
}
