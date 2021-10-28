package algorithms;

import api.PhemexAPI;
import java.util.LinkedList;

import org.json.JSONArray;
import org.json.JSONObject;

public abstract class Algorithm {

    private static Algorithm algorithm; // selected algorithm

    protected static boolean inPosition = false;

    static class Candle {
        double high, low, open, close, ohlc4, hlc3;
        long timestamp;
    }

    protected static double lastPrice;
    protected static LinkedList<Candle> candles = new LinkedList<Candle>();

    private static String side = "NA";
    private static int size = 0;
    private static double entryPrice = 0;
    private static double roi = 0;
    
    private static long ping;

    public String name;

    // All algorithms must implement these methods
    public abstract void onIncrementalUpdate();

    public abstract void onSnapshotUpdate();

    public abstract void initialize();

    public abstract JSONObject getAlgorithm();


    // Updates local candles when the websocket sends an update
    public static void incrementalUpdate(JSONArray candlesArray) {

        JSONArray candleArray = candlesArray.getJSONArray(0);
        Candle currentCandle = Algorithm.candles.getFirst();

        if (currentCandle.timestamp != candleArray.getLong(0)) { // New candle
            currentCandle = new Candle();
            Algorithm.candles.addFirst(currentCandle);
            Algorithm.candles.removeLast();
        }

        setCandle(candleArray, currentCandle);

        if (algorithm != null) {
            algorithm.onIncrementalUpdate();
        }
    }

    // On websocket connect, initialize the last 1000 candles
    public static void snapshotUpdate(JSONArray candlesArray) {

        Algorithm.candles.clear();

        for (int i = 0; i < candlesArray.length(); i++) {
            JSONArray candleArray = candlesArray.getJSONArray(i);
            Candle newCandle = new Candle();
            setCandle(candleArray, newCandle);
            Algorithm.candles.addLast(newCandle);
        }

        if (algorithm != null) {
            algorithm.onSnapshotUpdate();
        }
    }

    // Updates a Candle object by extracting values from a JSONArray
    private static void setCandle(JSONArray candleArray, Candle candle) {
        candle.timestamp = candleArray.getLong(0);
        candle.open = candleArray.getDouble(3) / 10000.0;
        candle.high = candleArray.getDouble(4) / 10000.0;
        candle.low = candleArray.getDouble(5) / 10000.0;
        candle.close = candleArray.getDouble(6) / 10000.0;
        candle.ohlc4 = (candle.open + candle.high + candle.low + candle.close) / 4;
        candle.hlc3 = (candle.high + candle.low + candle.close) / 3;

        Algorithm.lastPrice = candle.close;
        if (side.equals("Buy")) {
            double roi2 = (double)(lastPrice - entryPrice) *10000/ entryPrice;
            Algorithm.roi = Math.round(roi2)/100.0;
        }
        else if (side.equals("Sell")) {
            double roi2 = (double)(entryPrice - lastPrice)*10000 / entryPrice;
            Algorithm.roi = Math.round(roi2) / 100.0;
        }
    }

    // Closes position (called when a new algorithm is deployed)
    public static void closePosition() {
        if (Algorithm.inPosition) {
            String closeSide = side.equals("Buy") ? "Sell" : "Buy";
            PhemexAPI.sendOrder(closeSide, size, -1, -1);
        }
    }

    // Updates current position when the websocket sends an update
    public static void updatePosition(String side, int size, double entryPrice) {
        Algorithm.size = size;
        if (size == 0) {
            Algorithm.inPosition = false;
            Algorithm.side = "NA";
            Algorithm.entryPrice = 0;
            Algorithm.roi = 0;

        } else {
            Algorithm.inPosition = true;
            Algorithm.side = side;
            Algorithm.entryPrice = entryPrice;
        }
    }

    public static boolean inPosition() {
        return Algorithm.inPosition;
    }

    public static void setAlgorithm(Algorithm algorithm) {
        closePosition();
        Algorithm.algorithm = algorithm;
        algorithm.initialize();
    }

    public static void setPing(long ping) {
        Algorithm.ping = ping;
    }


    public static String getInfo() {
        JSONObject info = new JSONObject();

        if (algorithm == null) {
            info.put("algorithm", new JSONObject().put("Name", "NA"));
        } else {
           info.put("algorithm", algorithm.getAlgorithm()); 
        }
        
        info.put("price", lastPrice);
        info.put("ping_ms", ping);
        info.put("roi", roi);

        String position;
        switch (side) {
            case "Buy": {
                position = "Long " + size + " contracts @ $" + entryPrice;
                break;
            }
            case "Sell": {
                position = "Short " + size + " contracts @ $" + entryPrice;
                break;
            }
            default: {
                position = "NA";
                break;
            }
        }
        
        info.put("position", position);

        return info.toString();
}}
