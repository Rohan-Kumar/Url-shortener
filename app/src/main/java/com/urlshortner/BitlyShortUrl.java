package com.urlshortner;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by Rohan on 9/30/2016.
 */

public class BitlyShortUrl extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "UrlShortner";
    private String Response = "";
    private String longUrl = "";
    private String shortUrl = "";
    private Context context;
    private boolean isValid = false;

    BitlyShortUrl(String url, Context ctx) {
        longUrl = url;
        context = ctx;

    }

    @Override
    protected Void doInBackground(Void... voids) {

        URL url;
        try {
            url = new URL("https://api-ssl.bitly.com/v3/shorten?access_token=" + context.getResources().getString(R.string.bitlyAccessToken) + "&longUrl=" + URLEncoder.encode(longUrl, "utf-8"));
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

            Log.d(TAG, "https://api-ssl.bitly.com/v3/shorten?access_token=" + context.getResources().getString(R.string.bitlyAccessToken) + "&longUrl=" + URLEncoder.encode(longUrl, "utf-8"));

//            httpURLConnection.setDoInput(true);
//            httpURLConnection.setDoOutput(true);
//            httpURLConnection.setRequestMethod("GET");
//            httpURLConnection.setRequestProperty("Content-Type", "application/json");

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
            if (jsonObject.getInt("status_code") == 200) {
                JSONObject data = jsonObject.getJSONObject("data");
                shortUrl = data.getString("url");
                isValid = true;
            } else {
                isValid = false;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (isValid)
            MainActivity.showShortUrl(shortUrl);
        else
            Toast.makeText(context, "This url is not accepted...", Toast.LENGTH_SHORT).show();
    }
}

