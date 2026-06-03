package com.design.lab.trading.model;

import java.math.BigDecimal;
import java.time.Instant;

import com.design.lab.trading.enums.OrderStatus;
import com.design.lab.trading.enums.OrderType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@Builder
@ToString
@EqualsAndHashCode(of = "orderId")
public class Order {
    private final String orderId;
    private final String userId;
    private final OrderType orderType;
    private final String stockSymbol;
    private final int quantity;
    private final BigDecimal price;
    private final Instant acceptedTimestamp;

    @Setter
    private int remainingQuantity;

    @Setter
    private OrderStatus orderStatus;
}