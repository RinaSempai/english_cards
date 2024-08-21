package com.example.englishcards;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class WordDetailActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;

    private TextView textViewWord;
    private TextView textViewTranslation;
    private TextView textViewUsageExample;
    private TextView textViewExampleTranslation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_detail);

        dbHelper = new DatabaseHelper(this);
        textViewWord = findViewById(R.id.textViewWord);
        textViewTranslation = findViewById(R.id.textViewTranslation);
        textViewUsageExample = findViewById(R.id.textViewUsageExample);
        textViewExampleTranslation = findViewById(R.id.textViewExampleTranslation);

        int wordId = getIntent().getIntExtra("WORD_ID", -1);
        loadTranslation(wordId);
    }

    private void loadTranslation(int wordId) {
        Cursor cursor = dbHelper.getTranslations(wordId);

        if (cursor.moveToFirst()) {
            String word = cursor.getString(cursor.getColumnIndex("Words.word"));
            String translation = cursor.getString(cursor.getColumnIndex("Translations.translation"));
            String usageExample = cursor.getString(cursor.getColumnIndex("Translations.usage_example"));
            String exampleTranslation = cursor.getString(cursor.getColumnIndex("Translations.example_translation"));

            textViewWord.setText(word);
            textViewTranslation.setText("Перевод: " + translation);
            textViewUsageExample.setText("Пример использования: " + usageExample);
            textViewExampleTranslation.setText("Перевод примера: " + exampleTranslation);
        }
        cursor.close();
    }
}
