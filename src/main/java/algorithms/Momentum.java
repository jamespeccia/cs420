package algorithms;

import api.PhemexAPI;
import java.util.LinkedList;

import org.json.JSONObject;

public class Momentum extends Algorithm {

    private double delta, bound, tpsl, velocity;

    private class ContinuousPrice {
        double price;
        long timestamp;

        public ContinuousPrice(double price, long timestamp) {
            this.price = price;
            this.timestamp = timestamp;
        }
    }

    LinkedList<ContinuousPrice> continuousPrices; // contain all prices from price delta ms ago to now

    public Momentum(int delta, double bound, double tpsl) {
        this.delta = delta;
        this.bound = bound;
        this.tpsl = tpsl;
        this.continuousPrices = new LinkedList<ContinuousPrice>();
        this.name = "Momentum";
    }

    @Override
    public void initialize() {
        return;
    }

    @Override
    public void onSnapshotUpdate() {
        continuousPrices.clear();
        addPricePoint();
    }

    @Override
    public void onIncrementalUpdate() {
        addPricePoint();
    }

    public void addPricePoint() {
        long timestamp = System.currentTimeMillis();
        ContinuousPrice price = new ContinuousPrice(lastPrice, timestamp);
        continuousPrices.addFirst(price);

        boolean remove = false;
        for (int i = 0; i < continuousPrices.size(); i++) {
            long priceTimestamp = continuousPrices.get(i).timestamp;
            if (remove) {
                continuousPrices.removeLast();
            } else if (priceTimestamp + delta < timestamp) {
                remove = true;
            }
        }

        velocity = (price.price - continuousPrices.getLast().price) / (delta) * 1000; // $/s

        if (!Algorithm.inPosition() && velocity > bound) {
            PhemexAPI.sendOrder("Buy", 1, tpsl);
            Algorithm.size = 1;

        } else if (!Algorithm.inPosition() && velocity < -bound) {
            PhemexAPI.sendOrder("Sell", 1, tpsl);
            Algorithm.size = 1;
        }
    }

    @Override
    public JSONObject getParametersString() {
        JSONObject info = new JSONObject();
        info.put("name", name);
        info.put("details_string", "- Velocity:  " + Math.round(velocity * 100) / 100.0 + " ($/second)\n- Delta:  " + delta + "ms\n- Bound: "
                + bound * 100 + " ($/second)\n- TP/SL:  " + tpsl * 100 + "%");
        return info;
    }

}
