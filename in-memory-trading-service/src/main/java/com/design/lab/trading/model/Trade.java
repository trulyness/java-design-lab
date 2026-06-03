package com.design.lab.trading.model;

import java.math.BigDecimal;
import java.time.Instant;

import com.design.lab.trading.enums.OrderType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@Builder
@ToString
@EqualsAndHashCode(of = "tradeId")
public class Trade {

    private final String tradeId;
    private final OrderType tradeType;
    private final String buyerOrderId;
    private final String sellerOrderId;
    private final String stockSymbol;
    private final int quantity;
    private final BigDecimal price;
    private final Instant tradeTimestamp;
}