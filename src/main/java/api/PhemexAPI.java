package api;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import algorithms.Algorithm;

public class PhemexAPI {

    public static final String API_KEY = "ddbc60bc-756a-44c2-9fcf-18c8a424487b";
    public static final String SECRET = "UD89DwH6Jm5C0N0h3lKGeGnzmX9fNp_xyjqr8JP3k_liYjI5NzE5Ni1hNjdiLTRjZWUtYTNjMi00NDg4ODQ5M2Q0ZGU";
    public static final String BASE_ENDPOINT = "testnet-api.phemex.com";

    // Places a market order
    public static void sendOrder(String orderSide, int orderQuantity, double tpsl) {

        JSONObject body = new JSONObject();

        body.put("clOrdID", API_KEY);
        body.put("ordType", "Market");
        body.put("orderQty", orderQuantity);
        body.put("side", orderSide);
        body.put("symbol", "BTCUSD");

        if (tpsl > 0) {

            double lastPrice = Algorithm.getLastPrice();

            double tpEp = orderSide.equals("Buy") ? Math.round((1 + tpsl) * lastPrice)* 10000
                    : Math.round((1 - tpsl) * lastPrice) * 10000;
            double slEp = orderSide.equals("Buy") ? Math.round((1 - tpsl) * lastPrice) * 10000
                    : Math.round((1 + tpsl) * lastPrice) * 10000;
            
            body.put("takeProfitEp", tpEp);
            body.put("stopLossEp", slEp);
        }

        String URL = "https://" + BASE_ENDPOINT + "/orders";

        String expiry = String.valueOf(System.currentTimeMillis() * 1000);

        String signature = null;
        try {
            signature = sign("/orders" + expiry + body.toString(), SECRET);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            System.err.println(e.toString());
            return;
        }

        HttpPost httpPost = new HttpPost(URL);
        httpPost.setHeader("content-type", "application/json");
        httpPost.setHeader("x-phemex-access-token", API_KEY);
        httpPost.setHeader("x-phemex-request-expiry", expiry);
        httpPost.setHeader("x-phemex-request-signature", signature);
        httpPost.setEntity(new StringEntity(body.toString(), "UTF-8"));

        HttpClient httpClient = HttpClients.createMinimal();

        HttpResponse httpResponse = null;
        try {
            httpResponse = httpClient.execute(httpPost);
            // System.out.println(EntityUtils.toString(httpResponse.getEntity(), "UTF-8"));
        } catch (IOException e) {
            System.err.println("Error sending order!");
        }
    }

    public static String sign(String parameters, String SECRET) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(SECRET.getBytes(), "HmacSHA256");
        sha256_HMAC.init(secret_key);
        return Hex.encodeHexString(sha256_HMAC.doFinal(parameters.getBytes()));
    }
}