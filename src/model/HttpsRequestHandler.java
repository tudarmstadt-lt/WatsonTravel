package model;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;


public class HttpsRequestHandler extends RequestHandler {

    public static String execute(String address) {
        HttpsURLConnection httpsURLConnection = null;
        String content = null;
        try {
            httpsURLConnection = (HttpsURLConnection) stringToURL(address).openConnection();
            content = inputStreamToString(new BufferedInputStream(httpsURLConnection.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            if (httpsURLConnection != null)
                httpsURLConnection.disconnect();
        }
        return content;
    }

}
