package algorithms;

import api.PhemexAPI;
import java.util.LinkedList;

import org.json.JSONObject;

public class Momentum extends Algorithm {

    private double delta, bound, tpsl, velocity;

    LinkedList<ContinuousPrice> continuousPrices; // contain all prices from price delta ms ago to now

    public Momentum(int delta, double bound, double tpsl) {
        this.delta = delta;
        this.bound = bound;
        this.tpsl = tpsl;
        this.continuousPrices = new LinkedList<ContinuousPrice>();
        this.name = "Momentum";
    }

    @Override
    public void onIncrementalUpdate() {
        addPricePoint();
    }

    @Override
    public void onSnapshotUpdate() {
        continuousPrices.clear();
        addPricePoint();
    }

    @Override
    public void initialize() {
        return;
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

        if (!inPosition && velocity > bound) {
            PhemexAPI.sendOrder("Buy", 1, lastPrice*(1+tpsl), lastPrice * (1 - tpsl));
        }

        else if (!inPosition && velocity < -bound) {
            PhemexAPI.sendOrder("Sell", 1, lastPrice * (1 - tpsl), lastPrice * (1 + tpsl));
        }
    }

    private class ContinuousPrice {
        double price;
        long timestamp;

        public ContinuousPrice(double price, long timestamp) {
            this.price = price;
            this.timestamp = timestamp;
        }
    }

    @Override
    public JSONObject getAlgorithm() {
        JSONObject info = new JSONObject();
        info.put("name", name);
        info.put("details_string", "- Delta:  " + delta + "ms\n- Bound: " + bound + " ($/second)\n- TP/SL:  " + tpsl + "%");
        return info;
    }



}
