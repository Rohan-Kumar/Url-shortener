package com.urlshortner;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class QRActivity extends AppCompatActivity {

    ImageView qrImage;
    String shortUrl;
    Bitmap bmp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);

        qrImage = (ImageView) findViewById(R.id.qr);

        Intent intent = getIntent();
        shortUrl = intent.getStringExtra("shortUrl");

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

                    AlertDialog.Builder alert = new AlertDialog.Builder(QRActivity.this);
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
                                File f = new File(Environment.getExternalStorageDirectory()
                                        + File.separator + "Pictures" /*+ File.separator + "QR" */+ File.separator + shortUrl + ".jpg");
                                f.createNewFile();
                                FileOutputStream fo = new FileOutputStream(f);
                                fo.write(bytes.toByteArray());
                                fo.flush();
                                fo.close();
                                Toast.makeText(QRActivity.this, "Successfully stored", Toast.LENGTH_SHORT).show();
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
}
