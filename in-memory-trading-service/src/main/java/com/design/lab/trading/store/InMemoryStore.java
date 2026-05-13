package com.trading.store;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.trading.book.OrderBook;
import com.trading.model.Order;
import com.trading.model.Trade;
import com.trading.model.User;

public class InMemoryStore implements Store {
    final ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();
    final ConcurrentHashMap<String, Order> orders = new ConcurrentHashMap<>();
    final ConcurrentHashMap<String, Trade> trades = new ConcurrentHashMap<>();
    final ConcurrentHashMap<String, OrderBook> orderBooks = new ConcurrentHashMap<>();

    @Override
    public Optional<User> getUserById(final String userId) {
        return Optional.ofNullable(users.get(userId));
    }

    @Override
    public void saveUser(final User user) {
        users.put(user.getUserId(), user);
    }

    @Override
    public Optional<Order> getOrderById(final String orderId) {
        return Optional.ofNullable(orders.get(orderId));
    }

    @Override
    public void saveOrder(final Order order) {
        orders.put(order.getOrderId(), order);
    }

    @Override
    public void updateOrder(final Order order) {
        orders.put(order.getOrderId(), order);
    }

    @Override
    public Optional<Trade> getTradeById(final String tradeId) {
        return Optional.ofNullable(trades.get(tradeId));
    }

    @Override
    public void saveTrade(final Trade trade) {
        trades.put(trade.getTradeId(), trade);
    }

    @Override
    public List<Trade> getAllTrades() {
        return new ArrayList<>(trades.values());
    }

    @Override
    public Optional<OrderBook> getOrderBook(final String symbol) {
        return Optional.ofNullable(orderBooks.get(symbol));
    }

    @Override
    public OrderBook getOrCreateOrderBook(final String symbol) {
        return orderBooks.computeIfAbsent(symbol, OrderBook::new);
    }
}
