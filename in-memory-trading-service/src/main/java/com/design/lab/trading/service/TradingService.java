package com.trading.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;

import com.trading.book.OrderBook;
import com.trading.enums.OrderStatus;
import com.trading.enums.OrderType;
import com.trading.exceptions.OrderBookNotFoundException;
import com.trading.exceptions.OrderNotFoundException;
import com.trading.exceptions.UnauthorizedAccessException;
import com.trading.model.Order;
import com.trading.model.Trade;
import com.trading.store.Store;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class TradingService {
    protected final Store store;

    public TradingService(final Store store) {
        this.store = store;
    }

    public Order placeOrder(final String userId,
                            final OrderType orderType,
                            final String stockSymbol,
                            final int quantity,
                            final BigDecimal price) {
        final String normalizedSymbol = stockSymbol.trim().toUpperCase();

        final Order order = Order.builder()
                .orderId(UUID.randomUUID().toString())
                .userId(userId)
                .orderType(orderType)
                .stockSymbol(normalizedSymbol)
                .quantity(quantity)
                .price(price)
                .acceptedTimestamp(Instant.now())
                .remainingQuantity(quantity)
                .orderStatus(OrderStatus.ACCEPTED)
                .build();

        final OrderBook orderBook = store.getOrCreateOrderBook(normalizedSymbol);
        orderBook.getLock().lock();
        try {
            validateUserExists(userId);
            validateOrderRequest(orderType, normalizedSymbol, quantity, price);
            placeOrderInternal(order, orderBook);
        } finally {
            orderBook.getLock().unlock();
        }

        return order;
    }

    public Order modifyOrder(final String userId,
                             final String orderId,
                             final int newQuantity,
                             final BigDecimal newPrice) {
        final Order existingOrder = store.getOrderById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (!existingOrder.getUserId().equals(userId)) {
            throw new UnauthorizedAccessException("User not authorized to modify this order");
        }

        final Order replacementOrder = Order.builder()
            .orderId(UUID.randomUUID().toString())
            .userId(userId)
            .orderType(existingOrder.getOrderType())
            .stockSymbol(existingOrder.getStockSymbol())
            .quantity(newQuantity)
            .price(newPrice)
            .acceptedTimestamp(Instant.now())
            .remainingQuantity(newQuantity)
            .orderStatus(OrderStatus.ACCEPTED)
            .build();

        final OrderBook orderBook = store.getOrderBook(existingOrder.getStockSymbol())
            .orElseThrow(OrderBookNotFoundException::new);

        orderBook.getLock().lock();
        try {
            if (existingOrder.getOrderStatus() == OrderStatus.CANCELED
                    || existingOrder.getOrderStatus() == OrderStatus.EXECUTED) {
                throw new IllegalStateException("Order cannot be modified in current state");
            }

            validateOrderRequest(
                existingOrder.getOrderType(),
                existingOrder.getStockSymbol(),
                newQuantity,
                newPrice
            );
            cancelOrderInternal(existingOrder, orderBook);
            placeOrderInternal(replacementOrder, orderBook);
        } finally {
            orderBook.getLock().unlock();
        }

        return replacementOrder;
    }

    public Order cancelOrder(final String userId, final String orderId) {
        final Order order = store.getOrderById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (!order.getUserId().equals(userId)) {
            throw new UnauthorizedAccessException("User not authorized to cancel this order");
        }

        final OrderBook orderBook = store.getOrderBook(order.getStockSymbol())
                .orElseThrow(OrderBookNotFoundException::new);

        orderBook.getLock().lock();
        try {
            if (order.getOrderStatus() == OrderStatus.CANCELED
                    || order.getOrderStatus() == OrderStatus.EXECUTED) {
                throw new IllegalStateException("Order cannot be canceled in current state");
            }
            cancelOrderInternal(order, orderBook);
        } finally {
            orderBook.getLock().unlock();
        }

        return order;
    }

    public OrderStatus getOrderStatus(final String userId, final String orderId) {
        final Order order = store.getOrderById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (!order.getUserId().equals(userId)) {
            throw new UnauthorizedAccessException("User not authorized to view this order");
        }

        return order.getOrderStatus();
    }

    protected void validateUserExists(final String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID cannot be null or blank");
        }

        if (store.getUserById(userId).isEmpty()) {
            throw new IllegalArgumentException("User not found: " + userId);
        }
    }

    protected void validateOrderRequest(final OrderType orderType,
                                        final String stockSymbol,
                                        final int quantity,
                                        final BigDecimal price) {
        if (orderType == null) {
            throw new IllegalArgumentException("Order type cannot be null");
        }

        if (stockSymbol == null || stockSymbol.isBlank()) {
            throw new IllegalArgumentException("Stock symbol cannot be null or blank");
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than 0");
        }
    }

    protected void matchOrder(final OrderBook orderBook, final Order incoming) {
        final Map<BigDecimal, Deque<Order>> oppositeMap =
                incoming.getOrderType() == OrderType.BUY
                        ? orderBook.getSellOrdersByPrice()
                        : orderBook.getBuyOrdersByPrice();

        final Deque<Order> queue = oppositeMap.get(incoming.getPrice());

        if (queue == null) return;

        while (incoming.getRemainingQuantity() > 0 && !queue.isEmpty()) {
            final Order opposite = queue.peekFirst();

            final int tradeQty = Math.min(
                    incoming.getRemainingQuantity(),
                    opposite.getRemainingQuantity()
            );

            final String buyerOrderId = incoming.getOrderType() == OrderType.BUY
                    ? incoming.getOrderId()
                    : opposite.getOrderId();

            final String sellerOrderId = incoming.getOrderType() == OrderType.SELL
                    ? incoming.getOrderId()
                    : opposite.getOrderId();

            final Trade trade = Trade.builder()
                    .tradeId(UUID.randomUUID().toString())
                    .tradeType(incoming.getOrderType())
                    .buyerOrderId(buyerOrderId)
                    .sellerOrderId(sellerOrderId)
                    .stockSymbol(incoming.getStockSymbol())
                    .quantity(tradeQty)
                    .price(incoming.getPrice())
                    .tradeTimestamp(java.time.Instant.now())
                    .build();

            store.saveTrade(trade);

            incoming.setRemainingQuantity(incoming.getRemainingQuantity() - tradeQty);
            opposite.setRemainingQuantity(opposite.getRemainingQuantity() - tradeQty);

            if (opposite.getRemainingQuantity() == 0) {
                opposite.setOrderStatus(OrderStatus.EXECUTED);
                queue.pollFirst();
            } else {
                opposite.setOrderStatus(OrderStatus.PARTIALLY_EXECUTED);
            }

            store.updateOrder(opposite);

            if (incoming.getRemainingQuantity() == 0) {
                incoming.setOrderStatus(OrderStatus.EXECUTED);
            } else {
                incoming.setOrderStatus(OrderStatus.PARTIALLY_EXECUTED);
            }
        }

        if (queue.isEmpty()) {
            oppositeMap.remove(incoming.getPrice());
        }
    }

    protected void placeOrderInternal(final Order order, final OrderBook orderBook) {
        store.saveOrder(order);
        matchOrder(orderBook, order);

        if (order.getRemainingQuantity() > 0) {
            orderBook.addOrder(order);
        }
        store.updateOrder(order);
    }

    protected void cancelOrderInternal(final Order order, final OrderBook orderBook) {
        orderBook.removeOrder(order);
        order.setOrderStatus(OrderStatus.CANCELED);
        store.updateOrder(order);
    }

    protected Order buildAcceptedOrder(final String userId,
                                       final OrderType orderType,
                                       final String stockSymbol,
                                       final int quantity,
                                       final BigDecimal price) {
        return Order.builder()
                .orderId(UUID.randomUUID().toString())
                .userId(userId)
                .orderType(orderType)
                .stockSymbol(stockSymbol)
                .quantity(quantity)
                .price(price)
                .acceptedTimestamp(Instant.now())
                .remainingQuantity(quantity)
                .orderStatus(OrderStatus.ACCEPTED)
                .build();
    }
}
