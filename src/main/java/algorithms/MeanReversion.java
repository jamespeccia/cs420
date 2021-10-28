package algorithms;

import api.PhemexAPI;

import org.json.JSONObject;

public class MeanReversion extends Algorithm {

    private int length;
    private double percent, tpsl, movingAverage, sum;
    private Candle currentCandle;
    private double lastCandlePrice;

    public MeanReversion(int length, double percent, double tpsl) {
        this.length = length > 1000 ? 1000 : length;
        this.percent = percent;
        this.tpsl = tpsl;
        this.movingAverage = 0;
        this.currentCandle = candles.getFirst();
        this.lastCandlePrice = 0;
        this.name = "Mean Reversion";
    }

    @Override
    public void initialize() {

        sum = 0;
        for (int i = 0; i < length; i++) {
            sum += candles.get(i).close;
        }
        this.movingAverage = sum / length;
        this.currentCandle = candles.getFirst();
        lastCandlePrice = this.currentCandle.close;

    }

    @Override
    public void onIncrementalUpdate() {

        if (candles.getFirst().timestamp != currentCandle.timestamp) { // New candle
            this.currentCandle = candles.getFirst();
            sum -= candles.get(length).close;
            sum += currentCandle.close;
        } else {
            sum -= lastCandlePrice;
            sum += currentCandle.close;
        }

        lastCandlePrice = currentCandle.close;
        this.movingAverage = sum / length;

        if (!inPosition && lastPrice > movingAverage * (1 + percent)) { // Price is above the average, should come down
            PhemexAPI.sendOrder("Sell", 1, (1 - tpsl), lastPrice * (1 + tpsl));
        } else if (!inPosition && lastPrice < movingAverage * (1 - percent)) { // Price is below average, should go up
            PhemexAPI.sendOrder("Buy", 1, (1 + tpsl), lastPrice * (1 - tpsl));
        }
    }

    @Override
    public void onSnapshotUpdate() {
        initialize();
    }


    public JSONObject getAlgorithm() {
        JSONObject info = new JSONObject();
        info.put("Name", name);
        info.put("Length", length);
        info.put("Percent", percent);
        info.put("TPSL", tpsl);
        return info;
    }

}
