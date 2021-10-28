package webserver;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import webserver.routes.Index;
import webserver.routes.Strategy;

public class WebServer {

    private final HttpServer HTTP_SERVER;

    public WebServer(final int PORT) throws IOException {
        this.HTTP_SERVER = HttpServer.create(new InetSocketAddress(PORT), 0);
        this.HTTP_SERVER.createContext("/getInfo", new Index());
        this.HTTP_SERVER.createContext("/strategy", new Strategy());
        this.HTTP_SERVER.setExecutor(null);
    }

    public void start() {
        this.HTTP_SERVER.start();
        System.out.println("Running");
    }
}