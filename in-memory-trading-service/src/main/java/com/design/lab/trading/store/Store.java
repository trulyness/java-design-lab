package com.design.lab.trading.store;

import java.util.List;
import java.util.Optional;

import com.design.lab.trading.book.OrderBook;
import com.design.lab.trading.model.Order;
import com.design.lab.trading.model.Trade;
import com.design.lab.trading.model.User;

public interface Store {
    Optional<User> getUserById(final String userId);
    void saveUser(final User user);

    Optional<Order> getOrderById(final String orderId);
    void saveOrder(final Order order);
    void updateOrder(final Order order);

    Optional<Trade> getTradeById(final String tradeId);
    void saveTrade(final Trade trade);
    List<Trade> getAllTrades();

    Optional<OrderBook> getOrderBook(final String symbol);
    OrderBook getOrCreateOrderBook(final String symbol);

}
