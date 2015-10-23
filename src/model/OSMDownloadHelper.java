package model;


import android.os.AsyncTask;
import com.graphhopper.util.Unzipper;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public abstract class OSMDownloadHelper extends AsyncTask<URL,Void,Void> {

    private File folderPath;
    private boolean routingMap;
    private String name;

    @Override
    protected Void doInBackground(URL... params) {
        HttpURLConnection httpURLConnection = null;
        try {
            httpURLConnection = (HttpURLConnection) params[0].openConnection();
            InputStream inputStream = httpURLConnection.getInputStream();
            if(routingMap)
                unzip(inputStream);
            else
                createMapFile(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            httpURLConnection.disconnect();
        }
        return null;
    }

    protected abstract void onPostExecute(Void _nothing);

    public void downloadMap(File filesDir, String path, String name) {
        this.folderPath = filesDir;
        this.name = name;
        this.routingMap = false;
        this.execute(generateURLFromString("http://download.mapsforge.org/maps/"+path+".map"));
    }

    public void downloadRoutingMap(File filesDir, String name) {
        this.folderPath = filesDir;
        this.name = name;
        this.routingMap = true;
        this.execute(generateURLFromString("http://130.83.118.129/Watson/Maps/"+name+".zip"));
    }

    private void unzip(InputStream inputStream) {
        folderPath = new File(folderPath,"/maps/");
        if(!folderPath.exists())
            folderPath.mkdirs();
        File file = new File(folderPath,name);
        try {
            new Unzipper().unzip(inputStream, file, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createMapFile(InputStream inputStream) throws IOException {
        folderPath = new File(folderPath,"/maps/");
        if(!folderPath.exists())
            folderPath.mkdirs();
        File file = new File(folderPath,name+".map");
        OutputStream outStream = new FileOutputStream(file);
        byte[] buffer = new byte[8 * 1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, bytesRead);
        }
        outStream.close();
    }

    private URL generateURLFromString(String address) {
        URL url = null;
        try {
            url = new URL(address);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }
}
