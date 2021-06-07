package com.crypto.candles.models;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class Trade {

    private BigDecimal price;
    private BigDecimal quantity;
    private Side side;
    private Long id;
    private Instant timestamp;
    private String instrumentName;

    public enum Side {
        BUY, SELL
    }

}
