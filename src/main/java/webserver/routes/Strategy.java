package webserver.routes;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import org.json.JSONObject;

import algorithms.Algorithm;
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
            double threshold = 0;
            boolean error = false;

            try {
                delta = Integer.parseInt(request.getString("delta"));
            } catch (NumberFormatException e) {
                response = "Error: Unable to set delta value!";
                error = true;
            }

            try {
                threshold = Double.parseDouble(request.getString("threshold"));
            } catch (NumberFormatException e) {
                response = "Error: Unable to set threshold value!";
                error = true;
            }

            if (!error) {
                Algorithm momentum = new Momentum(delta, threshold, 1.0);
                Algorithm.setAlgorithm(momentum);
                response = "Success: Algorithm set successfully!";
            }
        }

    
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
    
}