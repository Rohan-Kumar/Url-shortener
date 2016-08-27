package com.urlshortner;

import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    RVAdapter adapter;
    SQLiteDatabase db;
    ArrayList<String> urlHistory = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        db = openOrCreateDatabase("UrlShortener", MODE_PRIVATE, null);

        setUpRecyclerView();
    }

    private void retrieveFromDB() {
        Cursor c = db.rawQuery("SELECT * FROM url",null);

        try {
            c.moveToFirst();

            do {
                urlHistory.add(c.getString(0));
            } while (c.moveToNext());
            adapter = new RVAdapter(HistoryActivity.this,urlHistory);
            recyclerView.setAdapter(adapter);

        } catch (CursorIndexOutOfBoundsException e){
            e.printStackTrace();
            Toast.makeText(HistoryActivity.this, "No url stored yet!", Toast.LENGTH_SHORT).show();
        }

    }

    private void setUpRecyclerView() {
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(HistoryActivity.this);
        recyclerView.setLayoutManager(layoutManager);

        retrieveFromDB();
    }
}
