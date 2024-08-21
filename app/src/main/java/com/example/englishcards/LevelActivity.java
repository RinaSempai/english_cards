package com.example.englishcards;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class LevelActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private ListView listViewWords;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level);

        dbHelper = new DatabaseHelper(this);
        listViewWords = findViewById(R.id.listViewWords);

        int levelId = getIntent().getIntExtra("LEVEL_ID", -1);
        loadWords(levelId);
    }

    private void loadWords(int levelId) {
        Cursor cursor = dbHelper.getWords(levelId);
        ArrayList<String> words = new ArrayList<>();
        ArrayList<Integer> wordIds = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                words.add(cursor.getString(cursor.getColumnIndex("word")));
                wordIds.add(cursor.getInt(cursor.getColumnIndex("id")));
            } while (cursor.moveToNext());
        }
        cursor.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, words);
        listViewWords.setAdapter(adapter);

        listViewWords.setOnItemClickListener((parent, view, position, id) -> {
            int wordId = wordIds.get(position);
            Intent intent = new Intent(LevelActivity.this, WordDetailActivity.class);
            intent.putExtra("WORD_ID", wordId);
            startActivity(intent);
        });
    }
}
