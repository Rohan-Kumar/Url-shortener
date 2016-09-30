package com.urlshortner;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by Rohan on 10/1/2016.
 */

public class TinyurlShortUrl extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "UrlShortner";
    private String Response = "";
    private String longUrl = "";
    private String shortUrl = "";

    TinyurlShortUrl(String url) {
        longUrl = url;
    }

    @Override
    protected Void doInBackground(Void... voids) {

        URL url;
        try {
            url = new URL("http://tinyurl.com/api-create.php?url=" + longUrl);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

            httpURLConnection.connect();
            BufferedReader mBufferedInputStream = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            String inline;
            while ((inline = mBufferedInputStream.readLine()) != null) {
                Response += inline;
            }
            mBufferedInputStream.close();
            Log.d("response", Response);
            parseResponse(Response);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    private void parseResponse(String Response) {
        Log.d(TAG, "parseResponse: " + Response);
        shortUrl = Response;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        MainActivity.showShortUrl(shortUrl);
    }
}


