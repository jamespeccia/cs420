import org.json.JSONArray;
import org.json.JSONObject;

import javax.websocket.*;

import algorithms.Algorithm;
import api.PhemexAPI;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Timer;
import java.util.TimerTask;

@ClientEndpoint
public class PhemexWebsocket {

    protected final WebSocketContainer CONTAINER;

    protected Session session;
    protected Timer timer;
    protected long sent;


    protected boolean isAuthenticated;

    public PhemexWebsocket() {
        this.CONTAINER = ContainerProvider.getWebSocketContainer();
    }

    public void connect() {
        boolean connected = false;
        while (!connected) {
            try {
                session = this.CONTAINER.connectToServer(this, new URI("wss://testnet.phemex.com/ws/"));
                connected = true;
                isAuthenticated = false;
                this.startHeartbeatService(3000);
                this.authenticate();
                Thread.sleep(1000);
                this.subscribe();
            } catch (DeploymentException | URISyntaxException | IOException | InterruptedException e) {
                System.err.println("Unable to connect to Phemex WebSocket. Attempting to reconnect in 5000ms.");
                connected = false;
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ie) {
                    System.err.println("Thread was interrupted while waiting to reconnect to Phemex WebSocket.");
                }
            }
        }
    }

    public void send(String message) throws IOException {
        session.getBasicRemote().sendText(message);
    }

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("Successfully connected to Phemex WebSocket.");
    }

    @OnMessage
    public void onMessage(Session session, String msg) {
        final JSONObject MESSAGE = new JSONObject(msg);

        // Account position update
        try {
            JSONObject data = (JSONObject) MESSAGE.getJSONArray("positions").get(0);
            if (data.getString("symbol").equals("BTCUSD")) {
                int size = data.getInt("size");
                String side = data.getString("side");
                double entryPrice = data.getInt("avgEntryPriceEp") / 10000.0;
                Algorithm.updatePosition(side, size, entryPrice);
            }

        } catch (Exception ignored) {
        }

        try {
            JSONArray data = (JSONArray) MESSAGE.getJSONArray("kline");
            boolean isSnapshot = MESSAGE.getString("type").equals("snapshot");
            if (isSnapshot) {
                Algorithm.snapshotUpdate(data);
            } else {
                Algorithm.incrementalUpdate(data);
            }

        } catch (Exception ignored) {
        }

    
        try {
            if(MESSAGE.getString("result").equals("pong")) {
                long recieved = System.currentTimeMillis();
                Algorithm.setPing(recieved - sent);
            }

        } catch (Exception ignored) {
        }

    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("Error occurred in Phemex WebSocket.");
        try {
            session.close();
        } catch (IOException e) {
            System.err.println("Error closing Phemex WebSocket.");
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        System.err.println("Phemex WebSocket closed.");
        this.connect();
    }

    public void startHeartbeatService(final int RATE) {
        final String HEARTBEAT = "{\"method\":\"server.ping\",\"params\":[],\"id\":21}";
        if (timer != null)
            timer.cancel();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    sent = System.currentTimeMillis();
                    send(HEARTBEAT);
                } catch (IOException e) {
                    System.err.println("Phemex heartbeat interrupted.");
                }
            }
        }, 0, RATE);
    }

    public void authenticate() {
        long expiry = System.currentTimeMillis() / 1000 + 5;

        String signature = null;
        try {
            signature = PhemexAPI.sign(PhemexAPI.API_KEY + expiry, PhemexAPI.SECRET);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            System.err.println("Error signing Phemex WebSocket authentication request. Authentication failed.");
        }

        final String AUTH_SUBSCRIPTION = "{\"method\": \"user.auth\", \"params\": [\"API\", \"" + PhemexAPI.API_KEY
                + "\", \"" + signature + "\", " + expiry + "], \"id\": 1234}";
        try {
            send(AUTH_SUBSCRIPTION);
        } catch (IOException e) {
            System.err.println("Phemex WebSocket authentication interrupted.");
        }
    }

    public void subscribe() {
        final String PRICE_SUBSCRIPTION = "{\"method\":\"kline.subscribe\",\"params\":[\"BTCUSD\",60],\"id\":0}";
        try {
            this.send(PRICE_SUBSCRIPTION);
        } catch (IOException e) {
            System.err.println("Phemex WebSocket price subscription interrupted.");
        }

        final String POSITION_SUBSCRIPTION = "{\"method\":\"aop.subscribe\",\"params\":[],\"id\":2}";
        try {
            this.send(POSITION_SUBSCRIPTION);
        } catch (IOException e) {
            System.err.println("Phemex WebSocket price subscription interrupted.");
        }
    }

}
