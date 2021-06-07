# Crypto.com API Reconciliation

This small project allows you to run some tests on the Crypto.com public API. With this tool, you can retrieve the
latest trades for a ticker and the candlesticks on a specific interval.

The project uses Spring Boot. The best way to build and explore it is to use IntelliJ or your favorite IDE. Otherwise,
you can also run

```
mvn clean install
```

or

```
mvnw clean install
```

This will generate the .jar in target directory and run the tests of the project.

If you want to play with the unit tests, change the data, etc. You should open it in your IDE and check the test
classes.

## Structure

### Project code

Using: Java 11

Configuration is in application.yaml from resources folder.

The dto package contains data classes that match the API HTTP responses. They are then converted to internal models for
convenience.

The service package contains the business logic, retrieving data from API, converting to our models and some convenience
methods to check data consistency.

### Tests

There are 2 classes, one to unit tests methods of the service CryptoComApiServiceTests, and CandlesApplicationTests.

- CryptoComApiServiceTests: unit tests to check methods in the service
- CandlesApplicationTests: some integration tests to try the service

#### Outputs

Sample outputs of :

```
Started CandlesApplicationTests in 0.877 seconds (JVM running for 1.422)
On ticker BTC_USDT and interval 1m: 1 valid candlesticks out of 600
On ticker BTC_USDT and interval 5m: 1 valid candlesticks out of 600
On ticker BTC_USDT and interval 15m: 0 valid candlesticks out of 600
On ticker BTC_USDT and interval 30m: 0 valid candlesticks out of 600
On ticker BTC_USDT and interval 1h: 0 valid candlesticks out of 600
On ticker BTC_USDT and interval 4h: 0 valid candlesticks out of 600
On ticker BTC_USDT and interval 6h: 0 valid candlesticks out of 525
On ticker BTC_USDT and interval 12h: 0 valid candlesticks out of 378
On ticker BTC_USDT and interval 1D: 0 valid candlesticks out of 259
On ticker BTC_USDT and interval 7D: 0 valid candlesticks out of 54
On ticker BTC_USDT and interval 14D: 0 valid candlesticks out of 29
On ticker BTC_USDT and interval 1M: 0 valid candlesticks out of 14
```

## Notes

- I chose to use BigDecimal for price comparisons instead of Double, to avoid precision loss.
- I also added a check on the volume, where I make sure the candlestick volume is equal to the sum of the quantity in
  all trades.
- Something unspecified in the exercise is what happens when the end time of the candlestick matches the trade time. In
  other words, are the candlestick boundaries included? I chose to exclude the start of the candle, but not the end:
  `if candlestick start time < trade time <= candlestick end time, return true`
- The algorithm to check the candlestick can be optimized, as it iterates over all trades for each candlestick to find
  the trades that match. It is O(n^2) on the CPU. It wouldn't be too difficult to make it a bit quicker, but I chose to
  make the code as clean and clear as possible.