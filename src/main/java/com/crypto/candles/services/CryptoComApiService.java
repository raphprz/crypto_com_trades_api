package com.crypto.candles.services;

import com.crypto.candles.config.CryptoComApiConfig;
import com.crypto.candles.dto.CandlestickApiResponse;
import com.crypto.candles.dto.TradesApiResponse;
import com.crypto.candles.models.Candlestick;
import com.crypto.candles.models.Trade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CryptoComApiService {
    // This service allows for easy requests to the server, handles response conversion
    // and also gives some utility functions to verify candles and trades

    private final RestTemplate restTemplate;
    private final CryptoComApiConfig cryptoComApiConfig;

    private static final List<String> allowedIntervals = List.of("1m", "5m", "15m", "30m", "1h", "4h", "6h", "12h", "1D", "7D", "14D", "1M");

    public List<String> getAllowedIntervals() {
        return Collections.unmodifiableList(allowedIntervals);
    }

    public List<Candlestick> getCandlestick(String instrumentName, String timeframe) {
        if (!StringUtils.hasText(instrumentName))
            throw new IllegalArgumentException("You need to specify an instrument name!");

        if (!StringUtils.hasText(timeframe))
            throw new IllegalArgumentException("You need to specify an timeframe!");

        String url = cryptoComApiConfig.getUrl() + "/public/get-candlestick";

        UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("instrument_name", instrumentName)
                .queryParam("timeframe", timeframe);

        CandlestickApiResponse response = restTemplate.getForEntity(uri.toUriString(), CandlestickApiResponse.class).getBody();

        if (response == null)
            // Not sure if an exception is better in that case
            return new ArrayList<>();

        Duration interval = parseInterval(response.getResult().getInterval());

        return response.getResult().getCandlesticks().stream()
                .map(candlestickDto -> Candlestick.builder()
                        .instrumentName(instrumentName)
                        .startTime(candlestickDto.getEndTime().minus(interval))
                        .endTime(candlestickDto.getEndTime())
                        .open(candlestickDto.getOpen())
                        .high(candlestickDto.getHigh())
                        .low(candlestickDto.getLow())
                        .close(candlestickDto.getClose())
                        .volume(candlestickDto.getVolume())
                        .build())
                .collect(Collectors.toList());
    }

    public List<Trade> getTrades() {
        // Returns all instruments
        return getTrades(null);
    }

    public List<Trade> getTrades(String instrumentName) {
        String url = cryptoComApiConfig.getUrl() + "/public/get-trades";

        UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(url);

        if (StringUtils.hasText(instrumentName))
            uri.queryParam("instrument_name", instrumentName);

        TradesApiResponse response = restTemplate.getForEntity(uri.toUriString(), TradesApiResponse.class).getBody();

        if (response == null)
            // Not sure if an exception is better in that case
            return new ArrayList<>();

        return response.getResult().getTrades().stream()
                .map(tradeDto -> Trade.builder()
                        .price(tradeDto.getPrice())
                        .quantity(tradeDto.getQuantity())
                        .side(Trade.Side.valueOf(tradeDto.getSide().toUpperCase()))
                        .id(tradeDto.getId())
                        .timestamp(tradeDto.getTimestamp())
                        .instrumentName(tradeDto.getInstrumentName())
                        .build())
                .collect(Collectors.toList());
    }

    public boolean isTradeInCandlestick(Trade trade, Candlestick candlestick) {
        // match if candlestick start time = trade time <= candlestick end time
        return trade.getInstrumentName().equals(candlestick.getInstrumentName())
                && trade.getTimestamp().isAfter(candlestick.getStartTime())  // exclude start time
                && !trade.getTimestamp().isAfter(candlestick.getEndTime());  // include end time
    }

    public List<Trade> filterTradesInCandlestick(List<Trade> trades, Candlestick candlestick) {
        return trades.stream()
                .sorted(Comparator.comparing(Trade::getTimestamp))
                .filter(trade -> isTradeInCandlestick(trade, candlestick))
                .collect(Collectors.toList());
    }

    public boolean isCandleStickValid(List<Trade> trades, Candlestick candlestick) {
        // A candlestick is valid when:
        // - open = price of the first trade
        // - close = price of the last trade
        // - high is the max price of all trades
        // - low is the min price of all trades
        // - volume is the sum of the quantity in all trades

        log.debug("Analyzing candlestick: {}", candlestick);

        // first, sort trades and exclude trade outside the candle
        List<Trade> filteredTrades = filterTradesInCandlestick(trades, candlestick);

        if (filteredTrades.isEmpty()) {
            log.debug("No trade found to match it!");
            // no trade covered by the candlestick
            return false;
        }

        BigDecimal volume = filteredTrades.stream()
                .map(Trade::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (volume.compareTo(candlestick.getVolume()) != 0)
            return false;

        BigDecimal open = filteredTrades.get(0).getPrice();
        if (open.compareTo(candlestick.getOpen()) != 0) {
            log.debug("Open does not match!");
            return false;
        }

        BigDecimal close = filteredTrades.get(filteredTrades.size() - 1).getPrice();
        if (close.compareTo(candlestick.getClose()) != 0) {
            log.debug("Close does not match!");
            return false;
        }

        BigDecimal high = filteredTrades.stream()
                .map(Trade::getPrice)
                .max(Comparator.naturalOrder())
                .orElseThrow(NoSuchElementException::new);
        if (high.compareTo(candlestick.getHigh()) != 0) {
            log.debug("High does not match!");
            return false;
        }

        BigDecimal low = filteredTrades.stream()
                .map(Trade::getPrice)
                .min(Comparator.naturalOrder())
                .orElseThrow(NoSuchElementException::new);
        if (low.compareTo(candlestick.getLow()) != 0) {
            log.debug("Low does not match!");
            return false;
        }

        log.debug("Candlestick is consistent.");
        return true;
    }

    public List<Candlestick> getValidCandlesticks(List<Trade> trades, List<Candlestick> candlesticks) {
        return candlesticks.stream()
                .filter(candlestick -> isCandleStickValid(trades, candlestick))
                .collect(Collectors.toList());
    }

    public Duration parseInterval(String interval) {
        if (interval == null || !allowedIntervals.contains(interval)) {
            String error = String.format("Interval must be one of these values: %s", allowedIntervals);
            throw new IllegalArgumentException(error);
        }

        int amount = Integer.parseInt(interval.substring(0, interval.length() - 1));
        char unit = interval.charAt(interval.length() - 1);

        if (unit == 'm')
            return Duration.ofMinutes(amount);

        if (unit == 'h')
            return Duration.ofHours(amount);

        if (unit == 'D')
            return Duration.ofDays(amount);

        if (unit == 'M')
            return Duration.ofDays(amount * 30L);

        throw new IllegalArgumentException("Interval could not be parsed!");
    }

}
