package com.crypto.candles.services;

import com.crypto.candles.models.Candlestick;
import com.crypto.candles.models.Trade;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
@SpringBootTest
public class CryptoComApiServiceTests {

    private final CryptoComApiService cryptoComApiService;

    @Autowired
    CryptoComApiServiceTests(CryptoComApiService cryptoComApiService) {
        this.cryptoComApiService = cryptoComApiService;
    }

    @Test
    void parseInterval_shouldParseProperly() {
        // Can also use a parametrized test from JUnit
        Duration duration1 = cryptoComApiService.parseInterval("1m");
        Duration duration2 = cryptoComApiService.parseInterval("5m");
        Duration duration3 = cryptoComApiService.parseInterval("15m");
        Duration duration4 = cryptoComApiService.parseInterval("30m");
        Duration duration5 = cryptoComApiService.parseInterval("1h");
        Duration duration6 = cryptoComApiService.parseInterval("4h");
        Duration duration7 = cryptoComApiService.parseInterval("6h");
        Duration duration8 = cryptoComApiService.parseInterval("7D");
        Duration duration9 = cryptoComApiService.parseInterval("1M");

        assertThat(duration1).isEqualTo(Duration.ofMinutes(1));
        assertThat(duration2).isEqualTo(Duration.ofMinutes(5));
        assertThat(duration3).isEqualTo(Duration.ofMinutes(15));
        assertThat(duration4).isEqualTo(Duration.ofMinutes(30));
        assertThat(duration5).isEqualTo(Duration.ofHours(1));
        assertThat(duration6).isEqualTo(Duration.ofHours(4));
        assertThat(duration7).isEqualTo(Duration.ofHours(6));
        assertThat(duration8).isEqualTo(Duration.ofDays(7));
        assertThat(duration9).isEqualTo(Duration.ofDays(30));
    }

    @Test
    void parseInterval_shouldThrow() {
        assertThatThrownBy(() -> cryptoComApiService.parseInterval("1Y")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> cryptoComApiService.parseInterval("")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> cryptoComApiService.parseInterval(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void isTradeInCandlestick_shouldReturnTrue() {
        Trade trade = Trade.builder()
                .timestamp(Instant.parse("2021-06-03T14:08:24Z"))
                .instrumentName("INST_NAME")
                .build();

        Candlestick candlestick = Candlestick.builder()
                .instrumentName("INST_NAME")
                .startTime(Instant.parse("2021-06-03T14:00:00Z"))
                .endTime(Instant.parse("2021-06-03T14:30:00Z"))
                .build();

        assertThat(cryptoComApiService.isTradeInCandlestick(trade, candlestick)).isTrue();
    }

    @Test
    void isTradeInCandlestick_shouldReturnFalse() {
        Trade trade = Trade.builder()
                .timestamp(Instant.parse("2021-06-03T15:08:24Z"))
                .instrumentName("INST_NAME")
                .build();

        Candlestick candlestick = Candlestick.builder()
                .instrumentName("INST_NAME")
                .startTime(Instant.parse("2021-06-03T14:00:00Z"))
                .endTime(Instant.parse("2021-06-03T14:30:00Z"))
                .build();

        assertThat(cryptoComApiService.isTradeInCandlestick(trade, candlestick)).isFalse();
    }

    @Test
    void filterTradesInCandlestick_shouldReturnEmptyList() {
        Trade trade1 = Trade.builder()
                .timestamp(Instant.parse("2021-06-03T15:08:24Z"))
                .instrumentName("INST_NAME")
                .build();

        Trade trade2 = Trade.builder()
                .timestamp(Instant.parse("2021-06-03T15:09:24Z"))
                .instrumentName("INST_NAME")
                .build();

        List<Trade> trades = List.of(trade1, trade2);

        Candlestick candlestick = Candlestick.builder()
                .instrumentName("INST_NAME")
                .startTime(Instant.parse("2021-06-03T14:00:00Z"))
                .endTime(Instant.parse("2021-06-03T14:30:00Z"))
                .build();

        List<Trade> filteredTrades = cryptoComApiService.filterTradesInCandlestick(trades, candlestick);

        assertThat(cryptoComApiService.filterTradesInCandlestick(trades, candlestick)).isEmpty();
        assertThat(filteredTrades).isNotEqualTo(trades);
    }

    @Test
    void filterTradesInCandlestick_shouldReturnListWithOneElement() {
        Trade trade1 = Trade.builder()
                .timestamp(Instant.parse("2021-06-03T14:08:24Z"))
                .instrumentName("INST_NAME")
                .build();

        Trade trade2 = Trade.builder()
                .timestamp(Instant.parse("2021-06-03T15:09:24Z"))
                .instrumentName("INST_NAME")
                .build();

        List<Trade> trades = List.of(trade1, trade2);

        Candlestick candlestick = Candlestick.builder()
                .instrumentName("INST_NAME")
                .startTime(Instant.parse("2021-06-03T14:00:00Z"))
                .endTime(Instant.parse("2021-06-03T14:30:00Z"))
                .build();

        List<Trade> filteredTrades = cryptoComApiService.filterTradesInCandlestick(trades, candlestick);

        assertThat(filteredTrades).hasSize(1);
        assertThat(filteredTrades).isNotEqualTo(trades);
    }

    @Test
    void filterTradesInCandlestick_shouldReturnListWithAllTrades() {
        Trade trade1 = Trade.builder()
                .timestamp(Instant.parse("2021-06-03T14:08:24Z"))
                .instrumentName("INST_NAME")
                .build();

        Trade trade2 = Trade.builder()
                .timestamp(Instant.parse("2021-06-03T14:09:24Z"))
                .instrumentName("INST_NAME")
                .build();

        List<Trade> trades = List.of(trade1, trade2);

        Candlestick candlestick = Candlestick.builder()
                .instrumentName("INST_NAME")
                .startTime(Instant.parse("2021-06-03T14:00:00Z"))
                .endTime(Instant.parse("2021-06-03T14:30:00Z"))
                .build();

        List<Trade> filteredTrades = cryptoComApiService.filterTradesInCandlestick(trades, candlestick);

        assertThat(filteredTrades).hasSize(2);
        assertThat(filteredTrades).isEqualTo(trades);
    }

    @Test
    void isCandleStickValid_SingleTrade_ShouldReturnTrue() {
        Trade trade1 = Trade.builder()
                .price(new BigDecimal(1))
                .quantity(new BigDecimal(5))
                .timestamp(Instant.parse("2021-06-03T14:08:24Z"))
                .instrumentName("INST_NAME")
                .build();

        List<Trade> trades = List.of(trade1);

        Candlestick candlestick = Candlestick.builder()
                .instrumentName("INST_NAME")
                .startTime(Instant.parse("2021-06-03T14:00:00Z"))
                .endTime(Instant.parse("2021-06-03T14:30:00Z"))
                .open(new BigDecimal(1))
                .high(new BigDecimal(1))
                .low(new BigDecimal(1))
                .close(new BigDecimal(1))
                .volume(new BigDecimal(5))
                .build();

        assertThat(cryptoComApiService.isCandleStickValid(trades, candlestick)).isTrue();
    }

    @Test
    void isCandleStickValid_SingleTrade_ShouldReturnFalseBecauseOpen() {
        Trade trade1 = Trade.builder()
                .price(new BigDecimal(1))
                .quantity(new BigDecimal(5))
                .timestamp(Instant.parse("2021-06-03T14:08:24Z"))
                .instrumentName("INST_NAME")
                .build();

        List<Trade> trades = List.of(trade1);

        Candlestick candlestick = Candlestick.builder()
                .instrumentName("INST_NAME")
                .startTime(Instant.parse("2021-06-03T14:00:00Z"))
                .endTime(Instant.parse("2021-06-03T14:30:00Z"))
                .open(new BigDecimal(2))
                .high(new BigDecimal(1))
                .low(new BigDecimal(1))
                .close(new BigDecimal(1))
                .volume(new BigDecimal(5))
                .build();

        assertThat(cryptoComApiService.isCandleStickValid(trades, candlestick)).isFalse();
    }

    @Test
    void isCandleStickValid_SingleTrade_ShouldReturnFalseBecauseHigh() {
        Trade trade1 = Trade.builder()
                .price(new BigDecimal(1))
                .quantity(new BigDecimal(5))
                .timestamp(Instant.parse("2021-06-03T14:08:24Z"))
                .instrumentName("INST_NAME")
                .build();

        List<Trade> trades = List.of(trade1);

        Candlestick candlestick = Candlestick.builder()
                .instrumentName("INST_NAME")
                .startTime(Instant.parse("2021-06-03T14:00:00Z"))
                .endTime(Instant.parse("2021-06-03T14:30:00Z"))
                .open(new BigDecimal(1))
                .high(new BigDecimal(2))
                .low(new BigDecimal(1))
                .close(new BigDecimal(1))
                .volume(new BigDecimal(5))
                .build();

        assertThat(cryptoComApiService.isCandleStickValid(trades, candlestick)).isFalse();
    }

    @Test
    void isCandleStickValid_SingleTrade_ShouldReturnFalseBecauseLow() {
        Trade trade1 = Trade.builder()
                .price(new BigDecimal(1))
                .quantity(new BigDecimal(5))
                .timestamp(Instant.parse("2021-06-03T14:08:24Z"))
                .instrumentName("INST_NAME")
                .build();

        List<Trade> trades = List.of(trade1);

        Candlestick candlestick = Candlestick.builder()
                .instrumentName("INST_NAME")
                .startTime(Instant.parse("2021-06-03T14:00:00Z"))
                .endTime(Instant.parse("2021-06-03T14:30:00Z"))
                .open(new BigDecimal(1))
                .high(new BigDecimal(1))
                .low(new BigDecimal(2))
                .close(new BigDecimal(1))
                .volume(new BigDecimal(5))
                .build();

        assertThat(cryptoComApiService.isCandleStickValid(trades, candlestick)).isFalse();
    }

    @Test
    void isCandleStickValid_SingleTrade_ShouldReturnFalseBecauseClose() {
        Trade trade1 = Trade.builder()
                .price(new BigDecimal(1))
                .quantity(new BigDecimal(5))
                .timestamp(Instant.parse("2021-06-03T14:08:24Z"))
                .instrumentName("INST_NAME")
                .build();

        List<Trade> trades = List.of(trade1);

        Candlestick candlestick = Candlestick.builder()
                .instrumentName("INST_NAME")
                .startTime(Instant.parse("2021-06-03T14:00:00Z"))
                .endTime(Instant.parse("2021-06-03T14:30:00Z"))
                .open(new BigDecimal(1))
                .high(new BigDecimal(1))
                .low(new BigDecimal(1))
                .close(new BigDecimal(2))
                .volume(new BigDecimal(5))
                .build();

        assertThat(cryptoComApiService.isCandleStickValid(trades, candlestick)).isFalse();
    }

    @Test
    void isCandleStickValid_SingleTrade_ShouldReturnFalseBecauseVolume() {
        Trade trade1 = Trade.builder()
                .price(new BigDecimal(1))
                .quantity(new BigDecimal(5))
                .timestamp(Instant.parse("2021-06-03T14:08:24Z"))
                .instrumentName("INST_NAME")
                .build();

        List<Trade> trades = List.of(trade1);

        Candlestick candlestick = Candlestick.builder()
                .instrumentName("INST_NAME")
                .startTime(Instant.parse("2021-06-03T14:00:00Z"))
                .endTime(Instant.parse("2021-06-03T14:30:00Z"))
                .open(new BigDecimal(1))
                .high(new BigDecimal(1))
                .low(new BigDecimal(1))
                .close(new BigDecimal(1))
                .volume(new BigDecimal(3))
                .build();

        assertThat(cryptoComApiService.isCandleStickValid(trades, candlestick)).isFalse();
    }

    @Test
    void isCandleStickValid_TwoTrades_ShouldReturnTrue() {
        Trade trade1 = Trade.builder()
                .price(new BigDecimal(1))
                .quantity(new BigDecimal(5))
                .timestamp(Instant.parse("2021-06-03T14:08:24Z"))
                .instrumentName("INST_NAME")
                .build();

        Trade trade2 = Trade.builder()
                .price(new BigDecimal(2))
                .quantity(new BigDecimal(3))
                .timestamp(Instant.parse("2021-06-03T14:09:24Z"))
                .instrumentName("INST_NAME")
                .build();

        List<Trade> trades = List.of(trade1, trade2);

        Candlestick candlestick = Candlestick.builder()
                .instrumentName("INST_NAME")
                .startTime(Instant.parse("2021-06-03T14:00:00Z"))
                .endTime(Instant.parse("2021-06-03T14:30:00Z"))
                .open(new BigDecimal(1))
                .high(new BigDecimal(2))
                .low(new BigDecimal(1))
                .close(new BigDecimal(2))
                .volume(new BigDecimal(8))
                .build();

        assertThat(cryptoComApiService.isCandleStickValid(trades, candlestick)).isTrue();
    }

    @Test
    void isCandleStickValid_TwoTrades_ShouldReturnFalseBecauseOpen() {
        Trade trade1 = Trade.builder()
                .price(new BigDecimal(1))
                .quantity(new BigDecimal(5))
                .timestamp(Instant.parse("2021-06-03T14:08:24Z"))
                .instrumentName("INST_NAME")
                .build();

        Trade trade2 = Trade.builder()
                .price(new BigDecimal(2))
                .quantity(new BigDecimal(3))
                .timestamp(Instant.parse("2021-06-03T14:09:24Z"))
                .instrumentName("INST_NAME")
                .build();

        List<Trade> trades = List.of(trade1, trade2);

        Candlestick candlestick = Candlestick.builder()
                .instrumentName("INST_NAME")
                .startTime(Instant.parse("2021-06-03T14:00:00Z"))
                .endTime(Instant.parse("2021-06-03T14:30:00Z"))
                .open(new BigDecimal(2))
                .high(new BigDecimal(2))
                .low(new BigDecimal(1))
                .close(new BigDecimal(2))
                .volume(new BigDecimal(8))
                .build();

        assertThat(cryptoComApiService.isCandleStickValid(trades, candlestick)).isFalse();
    }

    @Test
    void isCandleStickValid_TwoTrades_ShouldReturnFalseBecauseHigh() {
        Trade trade1 = Trade.builder()
                .price(new BigDecimal(1))
                .quantity(new BigDecimal(5))
                .timestamp(Instant.parse("2021-06-03T14:08:24Z"))
                .instrumentName("INST_NAME")
                .build();

        Trade trade2 = Trade.builder()
                .price(new BigDecimal(2))
                .quantity(new BigDecimal(3))
                .timestamp(Instant.parse("2021-06-03T14:09:24Z"))
                .instrumentName("INST_NAME")
                .build();

        List<Trade> trades = List.of(trade1, trade2);

        Candlestick candlestick = Candlestick.builder()
                .instrumentName("INST_NAME")
                .startTime(Instant.parse("2021-06-03T14:00:00Z"))
                .endTime(Instant.parse("2021-06-03T14:30:00Z"))
                .open(new BigDecimal(1))
                .high(new BigDecimal(1))
                .low(new BigDecimal(1))
                .close(new BigDecimal(2))
                .volume(new BigDecimal(8))
                .build();

        assertThat(cryptoComApiService.isCandleStickValid(trades, candlestick)).isFalse();
    }

    @Test
    void isCandleStickValid_TwoTrades_ShouldReturnFalseBecauseLow() {
        Trade trade1 = Trade.builder()
                .price(new BigDecimal(1))
                .quantity(new BigDecimal(5))
                .timestamp(Instant.parse("2021-06-03T14:08:24Z"))
                .instrumentName("INST_NAME")
                .build();

        Trade trade2 = Trade.builder()
                .price(new BigDecimal(2))
                .quantity(new BigDecimal(3))
                .timestamp(Instant.parse("2021-06-03T14:09:24Z"))
                .instrumentName("INST_NAME")
                .build();

        List<Trade> trades = List.of(trade1, trade2);

        Candlestick candlestick = Candlestick.builder()
                .instrumentName("INST_NAME")
                .startTime(Instant.parse("2021-06-03T14:00:00Z"))
                .endTime(Instant.parse("2021-06-03T14:30:00Z"))
                .open(new BigDecimal(1))
                .high(new BigDecimal(2))
                .low(new BigDecimal(2))
                .close(new BigDecimal(2))
                .volume(new BigDecimal(8))
                .build();

        assertThat(cryptoComApiService.isCandleStickValid(trades, candlestick)).isFalse();
    }

    @Test
    void isCandleStickValid_TwoTrades_ShouldReturnFalseBecauseClose() {
        Trade trade1 = Trade.builder()
                .price(new BigDecimal(1))
                .quantity(new BigDecimal(5))
                .timestamp(Instant.parse("2021-06-03T14:08:24Z"))
                .instrumentName("INST_NAME")
                .build();

        Trade trade2 = Trade.builder()
                .price(new BigDecimal(2))
                .quantity(new BigDecimal(3))
                .timestamp(Instant.parse("2021-06-03T14:09:24Z"))
                .instrumentName("INST_NAME")
                .build();

        List<Trade> trades = List.of(trade1, trade2);

        Candlestick candlestick = Candlestick.builder()
                .instrumentName("INST_NAME")
                .startTime(Instant.parse("2021-06-03T14:00:00Z"))
                .endTime(Instant.parse("2021-06-03T14:30:00Z"))
                .open(new BigDecimal(1))
                .high(new BigDecimal(2))
                .low(new BigDecimal(1))
                .close(new BigDecimal(1))
                .volume(new BigDecimal(8))
                .build();

        assertThat(cryptoComApiService.isCandleStickValid(trades, candlestick)).isFalse();
    }

    @Test
    void isCandleStickValid_TwoTrades_ShouldReturnFalseBecauseVolume() {
        Trade trade1 = Trade.builder()
                .price(new BigDecimal(1))
                .quantity(new BigDecimal(5))
                .timestamp(Instant.parse("2021-06-03T14:08:24Z"))
                .instrumentName("INST_NAME")
                .build();

        Trade trade2 = Trade.builder()
                .price(new BigDecimal(2))
                .quantity(new BigDecimal(3))
                .timestamp(Instant.parse("2021-06-03T14:09:24Z"))
                .instrumentName("INST_NAME")
                .build();

        List<Trade> trades = List.of(trade1, trade2);

        Candlestick candlestick = Candlestick.builder()
                .instrumentName("INST_NAME")
                .startTime(Instant.parse("2021-06-03T14:00:00Z"))
                .endTime(Instant.parse("2021-06-03T14:30:00Z"))
                .open(new BigDecimal(1))
                .high(new BigDecimal(2))
                .low(new BigDecimal(1))
                .close(new BigDecimal(2))
                .volume(new BigDecimal(5))
                .build();

        assertThat(cryptoComApiService.isCandleStickValid(trades, candlestick)).isFalse();
    }

}
