package algorithms;

import api.PhemexAPI;

import org.json.JSONObject;

public class MeanReversion extends Algorithm {

    private int length;
    private double bound, tpsl, movingAverage, sum;
    private Candle currentCandle;
    private double lastCandlePrice;

    public MeanReversion(int length, double bound, double tpsl) {
        this.length = length > 1000 ? 1000 : length;
        this.bound = bound;
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

        if (!Algorithm.inPosition() && lastPrice > movingAverage * (1 + bound)) { // Price is above the average, should
                                                                                  // come down
            PhemexAPI.sendOrder("Sell", 1, tpsl);
            Algorithm.size = 1;
        } else if (!Algorithm.inPosition() && lastPrice < movingAverage * (1 - bound)) { // Price is below average,
                                                                                         // should go up
            PhemexAPI.sendOrder("Buy", 1, tpsl);
            Algorithm.size = 1;
        }
    }

    @Override
    public void onSnapshotUpdate() {
        initialize();
    }

    public JSONObject getParametersString() {
        JSONObject info = new JSONObject();
        info.put("name", name);
        info.put("details_string", "- Moving Price:  $" + Math.round(movingAverage*100)/100.0 + "\n- Length:  " + length
                + " minutes\n- Bound: " + bound * 100 + "%\n- TP/SL:  " + tpsl * 100 + "%");
        return info;
    }

}
