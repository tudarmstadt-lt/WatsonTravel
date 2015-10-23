package model;


import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class RequestHandler {
    protected static String inputStreamToString(InputStream inputStream) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        int b;
        while ((b = inputStream.read()) != -1) {
            stringBuilder.append((char) b);
        }
        return stringBuilder.toString();
    }

    protected static URL stringToURL(String address) {
        URL url = null;
        try {
            url = new URL(address);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }
}
