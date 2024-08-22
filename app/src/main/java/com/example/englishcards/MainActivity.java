package com.example.englishcards;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private ListView listViewLevels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);

        try {
            dbHelper.createDatabase();
            dbHelper.openDatabase();
        } catch (IOException e) {
            throw new Error("Unable to create database");
        }

        listViewLevels = findViewById(R.id.listViewLevels);
        loadLevels();
    }

    private void loadLevels() {
        Cursor cursor = dbHelper.getLevels();
        ArrayList<String> levels = new ArrayList<>();
        ArrayList<Integer> levelIds = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                levels.add(cursor.getString(cursor.getColumnIndex("name")));
                levelIds.add(cursor.getInt(cursor.getColumnIndex("id")));
            } while (cursor.moveToNext());
        }
        cursor.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, levels);
        listViewLevels.setAdapter(adapter);

        listViewLevels.setOnItemClickListener((parent, view, position, id) -> {
            int levelId = levelIds.get(position);
            Intent intent = new Intent(MainActivity.this, LevelActivity.class);
            intent.putExtra("LEVEL_ID", levelId);
            startActivity(intent);
        });
    }
}
