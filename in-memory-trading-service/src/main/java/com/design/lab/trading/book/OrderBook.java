package com.trading.book;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import com.trading.enums.OrderType;
import com.trading.model.Order;

public class OrderBook {
    private final String symbol;

    private final Map<BigDecimal, Deque<Order>> buyOrdersByPrice;
    private final Map<BigDecimal, Deque<Order>> sellOrdersByPrice;

    private final ReentrantLock lock = new ReentrantLock();

    public OrderBook(String symbol) {
        this.symbol = symbol;
        this.buyOrdersByPrice = new HashMap<>();
        this.sellOrdersByPrice = new HashMap<>();
    }

    public String getSymbol() {
        return symbol;
    }

    public Map<BigDecimal, Deque<Order>> getBuyOrdersByPrice() {
        return buyOrdersByPrice;
    }

    public Map<BigDecimal, Deque<Order>> getSellOrdersByPrice() {
        return sellOrdersByPrice;
    }

    public void addOrder(Order order) {
        if (order.getOrderType() == OrderType.BUY) {
            Deque<Order> buyQueue = buyOrdersByPrice
                    .computeIfAbsent(order.getPrice(), p -> new ArrayDeque<>());
            buyQueue.addLast(order);
        } else {
            Deque<Order> sellQueue = sellOrdersByPrice
                    .computeIfAbsent(order.getPrice(), p -> new ArrayDeque<>());
            sellQueue.addLast(order);
        }
    }

    public void removeOrder(Order order) {
        Map<BigDecimal, Deque<Order>> sideMap =
                order.getOrderType() == OrderType.BUY ? buyOrdersByPrice : sellOrdersByPrice;
    
        Deque<Order> queue = sideMap.get(order.getPrice());
    
        if (queue != null) {
            queue.remove(order);
    
            if (queue.isEmpty()) {
                sideMap.remove(order.getPrice());
            }
        }
    }

    public ReentrantLock getLock() {
        return lock;
    }
}
