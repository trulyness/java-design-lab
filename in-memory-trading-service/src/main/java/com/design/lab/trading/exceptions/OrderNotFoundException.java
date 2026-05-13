package com.trading.exceptions;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(final String orderId) {
        super("Order not found: " + orderId);
    }
}
