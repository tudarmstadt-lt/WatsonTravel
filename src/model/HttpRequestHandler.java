package model;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpRequestHandler extends RequestHandler {

    private static final int CONNECTION_TIMEOUT = 5000;

    public static String execute(String address) {
        HttpURLConnection httpURLConnection = null;
        String content = null;
        try {
            httpURLConnection = (HttpURLConnection) stringToURL(address).openConnection();
            content = inputStreamToString(new BufferedInputStream(httpURLConnection.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            httpURLConnection.disconnect();
        }
        return content;
    }
}
