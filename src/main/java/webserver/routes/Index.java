package webserver.routes;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import algorithms.Algorithm;
import java.io.IOException;
import java.io.OutputStream;

public class Index implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        String response = Algorithm.getInfo();
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Content-Type", "application/json;");
        exchange.sendResponseHeaders(200, response.length());
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
