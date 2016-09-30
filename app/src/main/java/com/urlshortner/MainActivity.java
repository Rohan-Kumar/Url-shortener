package com.urlshortner;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    EditText longUrl;
    static TextView shortUrlTV;
    static LinearLayout copy, share, qr;
    static CardView card;
    static SQLiteDatabase db;
    private String[] api = {"Google url shortener","Bitly url shortener","Tiny url shortener"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        init();

        onClick();
    }

    private void onClick() {
        shortUrlTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(shortUrlTV.getText().toString()));
                startActivity(intent);
            }
        });

        copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipMan = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("UrlShortener", shortUrlTV.getText().toString());
                clipMan.setPrimaryClip(clip);
                Toast.makeText(MainActivity.this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, shortUrlTV.getText().toString());
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, "Url Shortener"));

            }
        });

        qr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, QRActivity.class);
                intent.putExtra("shortUrl", shortUrlTV.getText().toString());
                startActivity(intent);
            }
        });
    }

    private void init() {
        longUrl = (EditText) findViewById(R.id.LongUrl);
        shortUrlTV = (TextView) findViewById(R.id.ShortUrl);
        copy = (LinearLayout) findViewById(R.id.copy);
        share = (LinearLayout) findViewById(R.id.share);
        qr = (LinearLayout) findViewById(R.id.qr);
        card = (CardView) findViewById(R.id.card);
        card.setVisibility(View.INVISIBLE);

        db = openOrCreateDatabase("UrlShortener", MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS url(URL VARCHAR);");
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_history) {
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
            startActivity(intent);
        }else if (id == R.id.default_api){
            AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
            alert.setSingleChoiceItems(api, getSharedPreferences("URL",Context.MODE_PRIVATE).getInt("api",0), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    getSharedPreferences("URL",Context.MODE_PRIVATE).edit().putInt("api",which).apply();
                    dialog.dismiss();
                }
            });
            alert.setTitle("Select shortener method");
            alert.show();
        }else if (id == R.id.nav_share) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, "Hello!\nCheck this amazing app to shorten your url!\nDownload it at http://play.google.com/store/apps/details?id=" + getBaseContext().getPackageName());
            sendIntent.setType("text/plain");
            startActivity(Intent.createChooser(sendIntent, "Url Shortener"));

        } else if (id == R.id.nav_rate) {
            Uri uri = Uri.parse("market://details?id=" + getBaseContext().getPackageName());
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            // To count with Play market backstack, After pressing back button,
            // to taken back to our application, we need to add following flags to intent.
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            try {
                startActivity(goToMarket);
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=" + getBaseContext().getPackageName())));
            }

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void shortUrl(View view) {
        String urlText = longUrl.getText().toString();
        if (urlText.equals("") || urlText == null) {
            longUrl.setError("Cannot be empty!");
            longUrl.requestFocus();
            card.setVisibility(View.INVISIBLE);
        } else {
            urlText = urlText.replaceAll(" ","");
            if (!urlText.startsWith("http"))
                urlText = "http://" + urlText;
            int which = getSharedPreferences("URL",Context.MODE_PRIVATE).getInt("api",0);
            switch (which){
                case 0:
                    new GoogleShortUrl(urlText,MainActivity.this).execute();
                    break;
                case 1:
                    new BitlyShortUrl(urlText,MainActivity.this).execute();
                    break;
                case 2:
                    new TinyurlShortUrl(urlText).execute();
                    break;
                default:
                    new GoogleShortUrl(urlText,MainActivity.this).execute();
            }

        }
    }

    public static void showShortUrl(String shortUrl) {
        card.setVisibility(View.VISIBLE);
        shortUrlTV.setText(Html.fromHtml("<u><font color=\"#00C9FF\">" + shortUrl + "</font></u>"));

        String query = "INSERT INTO url VALUES('" + shortUrlTV.getText().toString() + "')";
        db.execSQL(query);

    }
}
