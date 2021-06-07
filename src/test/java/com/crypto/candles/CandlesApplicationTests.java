package com.crypto.candles;

import com.crypto.candles.models.Candlestick;
import com.crypto.candles.models.Trade;
import com.crypto.candles.services.CryptoComApiService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@SpringBootTest
class CandlesApplicationTests {

    private final CryptoComApiService cryptoComApiService;

    @Autowired
    CandlesApplicationTests(CryptoComApiService cryptoComApiService) {
        this.cryptoComApiService = cryptoComApiService;
    }

    void checkAllCandlesticks(String ticker, List<String> intervals) {
        List<Trade> trades = cryptoComApiService.getTrades(ticker).stream()
                .sorted(Comparator.comparing(Trade::getTimestamp).reversed())
                .collect(Collectors.toList());

        for (String interval : intervals) {
            List<Candlestick> candlesticks = cryptoComApiService.getCandlestick(ticker, interval).stream()
                    .sorted(Comparator.comparing(Candlestick::getEndTime).reversed())
                    .collect(Collectors.toList());

            List<Candlestick> validCandlesticks = cryptoComApiService.getValidCandlesticks(trades, candlesticks);

            log.info("On ticker {} and interval {}: {} valid candlesticks out of {}", ticker, interval, validCandlesticks.size(), candlesticks.size());
        }
    }

    @Test
    void checkAllCandlesticksAllIntervals() {
        String ticker = "VET_USDT";
        List<String> intervals = cryptoComApiService.getAllowedIntervals();

        checkAllCandlesticks(ticker, intervals);
    }

}
