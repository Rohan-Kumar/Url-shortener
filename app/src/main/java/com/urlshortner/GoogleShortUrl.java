package com.urlshortner;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by Rohan on 8/25/2016.
 */
public class GoogleShortUrl extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "UrlShortner";
    String Response = "";
    String longUrl = "";
    String shortUrl = "";

    GoogleShortUrl(String url) {
        longUrl = url;

    }

    @Override
    protected Void doInBackground(Void... voids) {

        URL url;
        try {
            url = new URL("https://www.googleapis.com/urlshortener/v1/url?key=AIzaSyCG3qtD9714DyIZrRjpyrdls9SZoI_tNyI");
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Content-Type", "application/json");

            OutputStream os = httpURLConnection.getOutputStream();

            BufferedWriter mBufferedWriter = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            mBufferedWriter.write("{\"longUrl\":\"" + longUrl + "\"}");
            mBufferedWriter.flush();
            mBufferedWriter.close();
            os.close();

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
        try {
            JSONObject jsonObject = new JSONObject(Response);
            shortUrl = jsonObject.getString("id");

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        MainActivity.showShortUrl(shortUrl);
    }
}
