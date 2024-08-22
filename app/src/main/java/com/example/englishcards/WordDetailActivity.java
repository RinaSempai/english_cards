package com.example.englishcards;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class WordDetailActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private TextView textViewTranslation;
    private TextView textViewUsageExample;
    private TextView textViewExampleTranslation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_detail);

        dbHelper = new DatabaseHelper(this);
        textViewTranslation = findViewById(R.id.textViewTranslation);
        textViewUsageExample = findViewById(R.id.textViewUsageExample);
        textViewExampleTranslation = findViewById(R.id.textViewExampleTranslation);

        int wordId = getIntent().getIntExtra("WORD_ID", -1);
        loadTranslation(wordId);
    }

    private void loadTranslation(int wordId) {
        Cursor cursor = dbHelper.getTranslations(wordId);

        if (cursor.moveToFirst()) {
            String translation = cursor.getString(cursor.getColumnIndex("translation"));
            String usageExample = cursor.getString(cursor.getColumnIndex("usage_example"));
            String exampleTranslation = cursor.getString(cursor.getColumnIndex("example_translation"));

            textViewTranslation.setText("Translation: " + translation);
            textViewUsageExample.setText("Usage Example: " + usageExample);
            textViewExampleTranslation.setText("Example Translation: " + exampleTranslation);
        }
        cursor.close();
    }
}
