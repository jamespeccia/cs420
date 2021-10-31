package webserver.routes;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import org.json.JSONObject;

import algorithms.Algorithm;
import algorithms.MeanReversion;
import algorithms.Momentum;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class Strategy implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        InputStream body = exchange.getRequestBody();
        InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(isr);
        String value = br.readLine();

        String response = "";

        JSONObject request = new JSONObject(value);
        String algorithm = request.getString("algorithm");

        if (algorithm.equals("momentum")) {
            int delta = 0;
            double bound = 0;
            double tpsl = 0;
            boolean error = false;

            try {
                delta = Integer.parseInt(request.getString("delta"));
            } catch (NumberFormatException e) {
                response = "Error: Unable to set delta value!";
                error = true;
            }

            try {
                bound = Double.parseDouble(request.getString("bound")) / 100;
            } catch (NumberFormatException e) {
                response = "Error: Unable to set bound value!";
                error = true;
            }

            try {
                tpsl = Double.parseDouble(request.getString("tpsl")) / 100;
                if (tpsl <= 0)
                    throw new NumberFormatException();
            } catch (NumberFormatException e) {
                response = "Error: Unable to set tpsl value!";
                error = true;
            }

            if (!error) {
                Algorithm momentum = new Momentum(delta, bound, tpsl);
                Algorithm.setAlgorithm(momentum);
                response = "Success: Algorithm set successfully!";
            }
        }

        else if (algorithm.equals("meanreversion")) {

            int length = 0;
            double bound = 0;
            double tpsl = 0;
            boolean error = false;

            try {
                length = Integer.parseInt(request.getString("length"));
            } catch (NumberFormatException e) {
                response = "Error: Unable to set delta value!";
                error = true;
            }

            try {
                bound = Double.parseDouble(request.getString("bound")) / 100;
            } catch (NumberFormatException e) {
                response = "Error: Unable to set threshold value!";
                error = true;
            }

            try {
                tpsl = Double.parseDouble(request.getString("tpsl")) / 100;
                if (tpsl <= 0)
                    throw new NumberFormatException();
            } catch (NumberFormatException e) {
                response = "Error: Unable to set tpsl value!";
                error = true;
            }

            if (!error) {
                Algorithm meanReversion = new MeanReversion(length, bound, tpsl);
                Algorithm.setAlgorithm(meanReversion);
                response = "Success: Algorithm set successfully!";
            }
        }

        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

}