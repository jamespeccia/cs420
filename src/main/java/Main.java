import algorithms.Algorithm;
import algorithms.MeanReversion;
import algorithms.Momentum;
import api.PhemexAPI;
import java.io.IOException;
import java.util.Scanner;
import webserver.WebServer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

class Main {
    public static void main(String[] args) {

        try {
            new WebServer(3000).start();
        } catch (IOException e) {
            System.out.println(e.toString());
        }

        Algorithm.closePosition();
        
        PhemexWebsocket phemexWebsocket = new PhemexWebsocket();
        phemexWebsocket.connect();

        Scanner in = new Scanner(System.in);

        boolean exit = false;
        while (!exit) {
            String input = in.nextLine();
            Algorithm meanRev = new MeanReversion(10, 10, 1);
            Algorithm.setAlgorithm(meanRev);
            String[] command = input.split(" ");

            switch (command[0]) {
                case "algorithm":
                    setAlgorithm(command);
                    break;
                case "order":
                    sendOrder(command);
                    break;
            }
        }

        in.close();
    }

    public static void setAlgorithm(String[] command) {
        switch (command[1]) {
            case "momentum": {
                int delta;
                try {
                    delta = Integer.parseInt(command[2]);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid delta value for momentum algorithm!");
                    return;
                }
                double threshold;
                try {
                    threshold = Double.parseDouble(command[3]);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid threshold value for momentum algorithm!");
                    return;
                }

                double tpsl;
                try {
                    tpsl = Double.parseDouble(command[4]);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid tpsl value for momentum algorithm!");
                    return;
                }

                Algorithm momentum = new Momentum(delta, threshold, tpsl);
                Algorithm.setAlgorithm(momentum);
                return;
            }

            case "meanreversion": {
                int length;
                try {
                    length = Integer.parseInt(command[2]);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid length value for mean reversion algorithm!");
                    return;
                }

                double percent;
                try {
                    percent = Double.parseDouble(command[3]);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid threshold value for mean reversion algorithm!");
                    return;
                }

                double tpsl;
                try {
                    tpsl = Double.parseDouble(command[4]);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid tpsl value for mean reversion algorithm!");
                    return;
                }

                Algorithm meanReversion = new MeanReversion(length, percent, tpsl);
                Algorithm.setAlgorithm(meanReversion);
                return;
            }

            default:
                System.err.println("Unknown algorithm");
        }
    }

    public static void sendOrder(String[] command) {

        String side = command[1];
        if (!(side.equals("Buy") || side.equals("Sell"))) {
            System.err.println("Invalid side!");
            return;
        }

        int size;
        try {
            size = Integer.parseInt(command[2]);
        } catch (NumberFormatException e) {
            System.err.println(command[2] + " is not a valid size!");
            return;
        }

        double takeProfit;
        try {
            takeProfit = Double.parseDouble(command[3]);
        } catch (NumberFormatException e) {
            System.err.println("$" + command[3] + " is not a valid take profit!");
            return;
        }

        double stopLoss;
        try {
            stopLoss = Double.parseDouble(command[4]);
        } catch (NumberFormatException e) {
            System.err.println("$" + command[4] + " is not a valid stop loss!");
            return;
        }

        if (takeProfit <= stopLoss) {
            System.err.println("Take profit must be greater than stop loss!");
            return;
        }

        PhemexAPI.sendOrder(side, size, takeProfit, stopLoss);
    }

    public static void buildWindow() {

        JFrame frame = new JFrame("Algorithmic Trading Bot");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        JButton button = new JButton("Press");
        JPanel assestInfo = new JPanel();
        JLabel asset = new JLabel("ETHUSD");
        JLabel lastPrice = new JLabel("Last Price: $12212.3");
        JLabel marketPrice = new JLabel("Market Price: $32321.2");
        assestInfo.add(asset);
        assestInfo.add(lastPrice);
        assestInfo.add(marketPrice);

        JTextArea output = new JTextArea(5, 20);

        output.setText("sdfjklsjfdsklfjksdljfsdlkfkj\nsfsdfdsfdsfds\nfsdfsdfsd\n");
        frame.getContentPane().add(assestInfo);
        frame.getContentPane().add(output);
        // Adds Button to content pane of frame
        frame.setVisible(true);
    }
}

// // final JSONObject CURRENT_STATE = CurrentState.initialize();
// // new WSMain(CURRENT_STATE);
// // try {
// // new WebServer(5000, CURRENT_STATE).start();
// // } catch (IOException e) {
// // Logger.log("Unable to start webserver.", e.toString());
// // }
// // //run(currentState);
// //
// // }
// //
// // public static void run(JSONObject currentState) {
// // final double AVERAGE = -2.5;
// // final double RADIUS = 10;
// //
// // final double UPPER = AVERAGE + RADIUS;
// // final double LOWER = AVERAGE - RADIUS;
// // boolean inTrade = false;
// //
// // BybitOrderThread bybitOrderThread;
// // PhemexOrderThread phemexOrderThread;
// //
// // while (true) {
// // try {
// // Thread.sleep(1000);
// // } catch (InterruptedException e) {
// // e.printStackTrace();
// // }
// //
// // // difference = bybit - phemex
// // double difference = 0.0;
// // try {
// // difference =
// currentState.getJSONObject("prices").getJSONObject("BTCUSD").getDouble("bybit")
// // -
// currentState.getJSONObject("prices").getJSONObject("BTCUSD").getDouble("phemex");
// // JSONObject response = PhemexClient.placeLimitOrder("BTCUSD", "Buy",
// currentState.getJSONObject("prices").getJSONObject("BTCUSD").getDouble("phemex"),
// 1, -1, -1);
// // break;
// // } catch (JSONException ignored) {
// // }

// // if (!inTrade && difference > UPPER) {
// // bybitOrderThread = new BybitOrderThread(currentState, false, 100);
// // phemexOrderThread = new PhemexOrderThread(currentState, true, 100);
// // bybitOrderThread.start();
// // phemexOrderThread.start();
// // inTrade = true;
// // } else if (!inTrade && difference < LOWER) {
// // bybitOrderThread = new BybitOrderThread(currentState, true, 100);
// // phemexOrderThread = new PhemexOrderThread(currentState, false, 100);
// // bybitOrderThread.start();
// // phemexOrderThread.start();
// // inTrade = true;
// // }
// }
// }
