package com.urlshortner;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    EditText longUrl;
    static TextView shortUrlTV;
    static LinearLayout copy, share, qr;
    static CardView card;
    static SQLiteDatabase db;
    private String[] api = {"Google url shortener", "Bitly url shortener", "Tiny url shortener"};
    ImageView qrImage;
    Bitmap bmp;

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
        TextView navTextView = (TextView) navigationView.getHeaderView(0).findViewById(R.id.navTextView);
        navTextView.setText(api[getSharedPreferences("URL", Context.MODE_PRIVATE).getInt("api", 0)]);

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
                requestPermission();
            }
        });
    }

    void requestPermission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 12345);
        else {
            qrImage.setVisibility(View.VISIBLE);
            generateQR(shortUrlTV.getText().toString());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 12345) {
            //If permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                qrImage.setVisibility(View.VISIBLE);
                generateQR(shortUrlTV.getText().toString());
            } else {
                //Displaying another toast if permission is not granted
                Toast.makeText(this, "Please grant permission to save the file", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }


    private void generateQR(final String shortUrl) {
        Toast.makeText(MainActivity.this, "Long press the QR to save it...", Toast.LENGTH_SHORT).show();
        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = writer.encode(shortUrl, BarcodeFormat.QR_CODE, 512, 512);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            qrImage.setImageBitmap(bmp);

        } catch (WriterException e) {
            e.printStackTrace();
        }

        qrImage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (bmp != null) {

                    AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                    alert.setTitle("SAVE??");
                    alert.setMessage("Do you want to save the generated QR code on to your device?");
                    alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                                bmp.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                                /*File file = new File(Environment.getExternalStorageDirectory()
                                        + File.separator + "Pictures/QR");
                                if (!file.exists()) {
                                    file.mkdirs();
                                }*/
                                String urlArray[] = shortUrl.split("/");
                                String urlString = urlArray[urlArray.length-1];
                                File f = new File(Environment.getExternalStorageDirectory()
                                        + File.separator + "Pictures" /*+ File.separator + "QR" */ + File.separator + urlString + ".jpg");
                                boolean create = f.createNewFile();
                                Log.d("TAG", "onClick: "+create);
                                FileOutputStream fo = new FileOutputStream(f);
                                fo.write(bytes.toByteArray());
                                fo.flush();
                                fo.close();
                                Toast.makeText(MainActivity.this, "Saved in Pictures folder...", Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                e.printStackTrace();
                                Log.d("Error", "catch");
                            }

                        }

                    });
                    alert.setNegativeButton("No", null);
                    alert.show();
                }

                return true;
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
        qrImage = (ImageView) findViewById(R.id.qrImage);
        qrImage.setVisibility(View.INVISIBLE);

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
        } else if (id == R.id.default_api) {
            AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
            alert.setSingleChoiceItems(api, getSharedPreferences("URL", Context.MODE_PRIVATE).getInt("api", 0), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    getSharedPreferences("URL", Context.MODE_PRIVATE).edit().putInt("api", which).apply();
                    NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
                    TextView navTextView = (TextView) navigationView.getHeaderView(0).findViewById(R.id.navTextView);
                    navTextView.setText(api[getSharedPreferences("URL", Context.MODE_PRIVATE).getInt("api", 0)]);
                    dialog.dismiss();
                }
            });
            alert.setTitle("Select shortener method");
            alert.show();
        } else if (id == R.id.nav_share) {
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
        card.setVisibility(View.INVISIBLE);
        qrImage.setVisibility(View.INVISIBLE);
        if (!hasConnection(MainActivity.this))
            Toast.makeText(MainActivity.this, "Please make sure you have network connection...", Toast.LENGTH_SHORT).show();
        else {
            String urlText = longUrl.getText().toString();
            if (urlText.equals("") || urlText == null) {
                longUrl.setError("Cannot be empty!");
                longUrl.requestFocus();
            } else {
                urlText = urlText.replaceAll(" ", "");
                if (!urlText.startsWith("http"))
                    urlText = "http://" + urlText;
                int which = getSharedPreferences("URL", Context.MODE_PRIVATE).getInt("api", 0);
                switch (which) {
                    case 0:
                        new GoogleShortUrl(urlText, MainActivity.this).execute();
                        break;
                    case 1:
                        new BitlyShortUrl(urlText, MainActivity.this).execute();
                        break;
                    case 2:
                        new TinyurlShortUrl(urlText).execute();
                        break;
                    default:
                        new GoogleShortUrl(urlText, MainActivity.this).execute();
                }

            }
        }
    }

    public static void showShortUrl(String shortUrl) {
        card.setVisibility(View.VISIBLE);
        shortUrlTV.setText(Html.fromHtml("<u><font color=\"#00C9FF\">" + shortUrl + "</font></u>"));

        String query = "INSERT INTO url VALUES('" + shortUrlTV.getText().toString() + "')";
        db.execSQL(query);

    }

    // checking if phone is connected to internet
    private boolean hasConnection(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo wifiNetwork = cm
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiNetwork != null && wifiNetwork.isConnected()) {
            return true;
        }

        NetworkInfo mobileNetwork = cm
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (mobileNetwork != null && mobileNetwork.isConnected()) {
            return true;
        }

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            return true;
        }

        return false;
    }

}
