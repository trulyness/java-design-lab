package com.design.lab.trading.service;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.design.lab.trading.enums.OrderStatus;
import com.design.lab.trading.enums.OrderType;
import com.design.lab.trading.model.Trade;
import com.design.lab.trading.model.User;
import com.design.lab.trading.store.InMemoryStore;

class TradingServiceTest {

    private InMemoryStore store;
    private TradingService service;

    @BeforeEach
    void setup() {
        store = new InMemoryStore();
        service = new TradingService(store);

        store.saveUser(User.builder().userId("u1").userName("Alice").build());
        store.saveUser(User.builder().userId("u2").userName("Bob").build());
    }

    @Test
    void testPlaceOrder() {
        var order = service.placeOrder("u1", OrderType.BUY, "AAPL", 10, new BigDecimal("100"));

        assertEquals(OrderStatus.ACCEPTED, order.getOrderStatus());
        assertEquals(10, order.getRemainingQuantity());
    }

    @Test
    void testFullMatch() {
        service.placeOrder("u1", OrderType.SELL, "AAPL", 10, new BigDecimal("100"));

        var buy = service.placeOrder("u2", OrderType.BUY, "AAPL", 10, new BigDecimal("100"));

        assertEquals(OrderStatus.EXECUTED, buy.getOrderStatus());

        List<Trade> trades = store.getAllTrades();
        assertEquals(1, trades.size());
        assertEquals(10, trades.get(0).getQuantity());
    }

    @Test
    void testPartialMatch() {

        var sell = service.placeOrder("u1", OrderType.SELL, "AAPL", 10, new BigDecimal("100"));

        assertEquals(OrderStatus.ACCEPTED, sell.getOrderStatus());
        assertEquals(10, sell.getRemainingQuantity());

        var buy = service.placeOrder("u2", OrderType.BUY, "AAPL", 5, new BigDecimal("100"));

        assertEquals(OrderStatus.EXECUTED, buy.getOrderStatus());
        assertEquals(0, buy.getRemainingQuantity());

        var updatedSell = store.getOrderById(sell.getOrderId()).orElseThrow();
        assertEquals(OrderStatus.PARTIALLY_EXECUTED, updatedSell.getOrderStatus());
        assertEquals(5, updatedSell.getRemainingQuantity());

        List<Trade> trades = store.getAllTrades();
        assertEquals(1, trades.size());
        assertEquals(5, trades.get(0).getQuantity());
    }

    @Test
    void testFIFOMatching() {

        var sell1 = service.placeOrder("u1", OrderType.SELL, "AAPL", 10, new BigDecimal("100"));
        var sell2 = service.placeOrder("u1", OrderType.SELL, "AAPL", 5, new BigDecimal("100"));

        service.placeOrder("u2", OrderType.BUY, "AAPL", 12, new BigDecimal("100"));

        final List<Trade> trades = store.getAllTrades().stream()
                .sorted(Comparator.comparing(Trade::getTradeTimestamp))
                .toList();

        assertEquals(2, trades.size());

        assertEquals(10, trades.get(0).getQuantity());
        assertEquals(sell1.getOrderId(), trades.get(0).getSellerOrderId());

        assertEquals(2, trades.get(1).getQuantity());
        assertEquals(sell2.getOrderId(), trades.get(1).getSellerOrderId());
    }

    @Test
    void testCancelOrder() {
        var order = service.placeOrder("u1", OrderType.BUY, "AAPL", 10, new BigDecimal("100"));

        service.cancelOrder("u1", order.getOrderId());

        assertEquals(OrderStatus.CANCELED, store.getOrderById(order.getOrderId()).get().getOrderStatus());
    }

    @Test
    void testModifyOrder() {
        var order = service.placeOrder("u1", OrderType.BUY, "AAPL", 10, new BigDecimal("100"));

        var modified = service.modifyOrder("u1", order.getOrderId(), 5, new BigDecimal("100"));

        assertNotEquals(order.getOrderId(), modified.getOrderId());
        assertEquals(5, modified.getQuantity());
    }

    @Test
    void testConcurrentOrders() throws Exception {

        int threads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threads);

        for (int i = 0; i < 5; i++) {
            executor.submit(() ->
                service.placeOrder("u1", OrderType.SELL, "AAPL", 10, new BigDecimal("100"))
            );
        }

        for (int i = 0; i < 5; i++) {
            executor.submit(() ->
                service.placeOrder("u2", OrderType.BUY, "AAPL", 10, new BigDecimal("100"))
            );
        }

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        List<Trade> trades = store.getAllTrades();

        assertEquals(5, trades.size());

        List<Integer> quantities = trades.stream()
            .map(Trade::getQuantity)
            .sorted()
            .toList();

        assertEquals(List.of(10, 10, 10, 10, 10), quantities);
    }

    @Test
    void testPlaceOrderWithZeroQuantity_shouldThrowException() {

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> service.placeOrder("u1", OrderType.BUY, "AAPL", 0, new BigDecimal("100"))
        );

        assertEquals("Quantity must be greater than 0", ex.getMessage());
    }
}