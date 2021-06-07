package com.crypto.candles.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TradesApiResponse {

    private Result result;

    @Data
    public static class Result {

        @JsonProperty("data")
        private List<Trade> trades;

    }

    @Data
    public static class Trade {

        @JsonProperty("p")
        private BigDecimal price;
        @JsonProperty("q")
        private BigDecimal quantity;
        @JsonProperty("s")
        private String side;
        @JsonProperty("d")
        private Long id;
        @JsonProperty("t")
        private Instant timestamp;
        @JsonProperty("i")
        private String instrumentName;

    }

}
