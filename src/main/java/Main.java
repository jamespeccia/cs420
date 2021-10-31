import algorithms.Algorithm;
import algorithms.MeanReversion;
import algorithms.Momentum;
import api.PhemexAPI;
import java.io.IOException;
import java.util.Scanner;
import webserver.WebServer;

class Main {
    public static void main(String[] args) {

        try {
            new WebServer(4000).start();
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

        double tpsl;
        try {
            tpsl = Double.parseDouble(command[3]);
        } catch (NumberFormatException e) {
            System.err.println(command[3] + " is not a valid percentage!");
            return;
        }

        PhemexAPI.sendOrder(side, size, tpsl);
    }
}
