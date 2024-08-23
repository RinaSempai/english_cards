package com.example.englishcards;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
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
        ArrayList<WordItem> words = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                int wordId = cursor.getInt(cursor.getColumnIndex("id"));
                String word = cursor.getString(cursor.getColumnIndex("word"));
                boolean isKnown = dbHelper.isWordKnown(wordId);
                words.add(new WordItem(wordId, word, isKnown));
            } while (cursor.moveToNext());
        }
        cursor.close();

        WordAdapter adapter = new WordAdapter(this, words, dbHelper);
        listViewWords.setAdapter(adapter);
    }
}
