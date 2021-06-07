package com.crypto.candles.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CandlestickApiResponse {

    private Result result;

    @Data
    public static class Result {

        @JsonProperty("instrument_name")
        private String instrumentName;
        private String interval;
        @JsonProperty("data")
        private List<Candlestick> candlesticks;

    }

    @Data
    public static class Candlestick {

        @JsonProperty("t")
        private Instant endTime;
        @JsonProperty("o")
        private BigDecimal open;
        @JsonProperty("h")
        private BigDecimal high;
        @JsonProperty("l")
        private BigDecimal low;
        @JsonProperty("c")
        private BigDecimal close;
        @JsonProperty("v")
        private BigDecimal volume;

    }

}
